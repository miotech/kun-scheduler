package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.workerInstance.WorkerInstanceDao;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.local.MiscService;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.*;

@Singleton
public class PodLifeCycleManager extends WorkerLifeCycleManager {

    private final Logger logger = LoggerFactory.getLogger(PodLifeCycleManager.class);
    private final KubernetesClient kubernetesClient;
    private final OperatorDao operatorDao;
    private final String POD_WORK_DIR = "/server/target";
    private final String POD_JAR_LIB = "/server/libs";

    @Inject
    public PodLifeCycleManager(TaskRunDao taskRunDao, WorkerInstanceDao workerInstanceDao,
                               WorkerMonitor workerMonitor, Props props, MiscService miscService,
                               KubernetesClient kubernetesClient, OperatorDao operatorDao) {
        super(workerInstanceDao, taskRunDao, workerMonitor, props, miscService);
        this.kubernetesClient = kubernetesClient;
        this.operatorDao = operatorDao;
    }

    @Override
    public WorkerSnapshot startWorker(TaskAttempt taskAttempt) {
        logger.info("going to start pod taskAttemptId = {}", taskAttempt.getId());
        Pod pod = kubernetesClient.pods()
                //todo :support different namespace
                .inNamespace(props.getString("executor.env.namespace"))
                .create(buildPod(taskAttempt));
        return PodStatusSnapShot.fromPod(pod);
    }

    @Override
    public Boolean stopWorker(Long taskAttemptId) {
        logger.info("going to stop pod taskAttemptId = {}", taskAttemptId);
        return kubernetesClient.pods()
                .inNamespace(props.getString("executor.env.namespace"))
                .withLabel(KUN_WORKFLOW)
                .withLabel(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttemptId))
                .delete();
    }

    @Override
    public WorkerSnapshot getWorker(Long taskAttemptId) {
        PodList podList = kubernetesClient.pods()
                .inNamespace(props.getString("executor.env.namespace"))
                .withLabel(KUN_WORKFLOW)
                .withLabel(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttemptId))
                .list();
        if (podList.getItems().size() > 1) {
            throw new IllegalStateException("found two pod with taskAttempt");
        }
        if (podList.getItems().size() == 0) {
            return null;
        }
        return PodStatusSnapShot.fromPod(podList.getItems().get(0));

    }


    private Pod buildPod(TaskAttempt taskAttempt) {
        Pod pod = new Pod();
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(KUN_WORKFLOW + taskAttempt.getId());
        objectMeta.setNamespace(props.getString("executor.env.namespace"));
        Map<String, String> labels = new HashMap<>();
        labels.put(KUN_WORKFLOW, null);
        labels.put(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttempt.getId()));
        objectMeta.setLabels(labels);
        pod.setMetadata(objectMeta);
        pod.setSpec(buildSpec(taskAttempt));
        return pod;
    }

    private PodSpec buildSpec(TaskAttempt taskAttempt) {
        logger.debug("building pod spec,taskAttemptId = {}", taskAttempt.getId());
        taskAttempt.getTaskRun().getTask().getOperatorId();
        PodSpec podSpec = new PodSpec();
        podSpec.setRestartPolicy("Never");
        podSpec.setContainers(Arrays.asList(buildContainer(taskAttempt)));
        if (props.containsKey("executor.env.privateHub")) {
            if (props.getBoolean("executor.env.privateHub.useSecret", false)) {
                LocalObjectReference secret = new LocalObjectReferenceBuilder()
                        .withName(props.getString("executor.env.privateHub.secert"))
                        .build();
                podSpec.setImagePullSecrets(Arrays.asList(secret));
            }
        }
        Volume nfsVolume = new VolumeBuilder()
                .withPersistentVolumeClaim(
                        new PersistentVolumeClaimVolumeSourceBuilder()
                                .withNewClaimName(props.getString("executor.env.nfsClaimName"))
                                .build())
                .withName(props.getString("executor.env.nfsName"))
                .build();
        List<Volume> volumeList = new ArrayList<>();
        volumeList.add(nfsVolume);
        podSpec.setVolumes(volumeList);
        return podSpec;
    }

    private Container buildContainer(TaskAttempt taskAttempt) {
        logger.debug("building pod container,taskAttemptId = {}", taskAttempt.getId());
        Long operatorId = taskAttempt.getTaskRun().getTask().getOperatorId();
        Container container = new Container();
        container.setImagePullPolicy(IMAGE_PULL_POLICY);
        String containerName = getContainerFromOperator(operatorId);
        String imageName = "kunoperator";
        if (props.containsKey("executor.env.privateHub")) {
            imageName = props.getString("executor.env.privateHub.url") + "/" + imageName;
        }
        container.setName(containerName);
        container.setImage(imageName);
        container.setCommand(buildCommand(taskAttempt.getId()));
        List<VolumeMount> mounts = new ArrayList<>();
        VolumeMount logMount = new VolumeMount();
        logMount.setMountPath(POD_WORK_DIR + "/logs");
        logMount.setName(props.getString("executor.env.nfsName"));
        logMount.setSubPath(props.getString("executor.env.logPath"));
        mounts.add(logMount);
        VolumeMount jarMount = new VolumeMount();
        jarMount.setMountPath(POD_JAR_LIB);
        jarMount.setName(props.getString("executor.env.nfsName"));
        jarMount.setSubPath(props.getString("resource.libDirectory"));
        mounts.add(jarMount);
        container.setVolumeMounts(mounts);
        container.setEnv(buildEnv(taskAttempt));
        return container;
    }

    private List<EnvVar> buildEnv(TaskAttempt taskAttempt) {
        logger.debug("building pod env,taskAttemptId = {}", taskAttempt.getId());
        Operator operatorDetail = operatorDao.fetchById(taskAttempt.getTaskRun().getTask().getOperatorId())
                .orElseThrow(EntityNotFoundException::new);
        List<EnvVar> envVarList = new ArrayList<>();
        //add config
        Map<String, Object> configMap = taskAttempt.getTaskRun().getConfig().getValues();
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String value = entry.getValue().toString();
            if (entry.getValue() instanceof List) {
                List<String> valueList = coverObjectToList(entry.getValue(), String.class);
                value = valueList.stream().collect(Collectors.joining(","));
            }
            EnvVar envVar = new EnvVar();
            envVar.setName(entry.getKey());
            envVar.setValue(value);
            envVarList.add(envVar);
        }
        String configKey = envVarList.stream().map(EnvVar::getName).collect(Collectors.joining(","));
        addVar(envVarList, "configKey", configKey);
        addVar(envVarList, "logPath", taskAttempt.getLogPath());
        addVar(envVarList, "taskAttemptId", taskAttempt.getId().toString());
        addVar(envVarList, "taskRunId", taskAttempt.getTaskRun().getId().toString());
        addVar(envVarList, "className", operatorDetail.getClassName());
        addVar(envVarList, "jarPath", "file:///server/libs/" + operatorDetail.getId() + "/" + operatorDetail.getName() + ".jar");
        return envVarList;
    }

    private void addVar(List<EnvVar> envVarList, String name, String value) {
        EnvVar envVar = new EnvVar();
        envVar.setName(name);
        envVar.setValue(value);
        envVarList.add(envVar);
    }

    private <T> List<T> coverObjectToList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }


    private String getContainerFromOperator(Long operatorId) {
        return "kun" + operatorDao.fetchById(operatorId).get().getName().replaceAll("-", "").toLowerCase();
    }

    private List<String> buildCommand(Long taskAttemptId) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.addAll(buildJVMArgs(taskAttemptId));
        command.add("-jar");
        command.add("/server/target/kubernetesOperatorLauncher.jar");
        return command;
    }

    private List<String> buildJVMArgs(Long taskAttemptId) {
        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-XX:+PrintGCDetails");
        jvmArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
        jvmArgs.add(String.format("-XX:HeapDumpPath=/tmp/%d/heapdump.hprof", taskAttemptId));
        return jvmArgs;
    }
}
