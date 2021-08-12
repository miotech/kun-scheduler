package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.local.MiscService;
import com.miotech.kun.workflow.worker.JsonCodec;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.*;

@Singleton
public class PodLifeCycleManager extends WorkerLifeCycleManager {

    private final Logger logger = LoggerFactory.getLogger(PodLifeCycleManager.class);
    private final KubernetesClient kubernetesClient;
    private final OperatorDao operatorDao;
    private final String POD_WORK_DIR = "/server/target";
    private final String POD_LIB_DIR = "/server/lib";
    private final Integer DB_MAX_POOL = 1;
    private final Integer MINI_MUM_IDLE = 0;

    @Inject
    public PodLifeCycleManager(TaskRunDao taskRunDao, WorkerMonitor workerMonitor, Props props, MiscService miscService,
                               KubernetesClient kubernetesClient, OperatorDao operatorDao, AbstractQueueManager queueManager) {
        super(taskRunDao, workerMonitor, props, miscService, queueManager);
        this.kubernetesClient = kubernetesClient;
        this.operatorDao = operatorDao;
    }

    @Override
    public WorkerSnapshot startWorker(TaskAttempt taskAttempt) {
        logger.info("going to start pod taskAttemptId = {}", taskAttempt.getId());
        Pod pod = kubernetesClient.pods()
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

    @Override
    public String getWorkerLog(Long taskAttemptId, Integer tailLines) {
        WorkerSnapshot workerSnapshot = getWorker(taskAttemptId);
        if (workerSnapshot != null && !workerSnapshot.getStatus().isFinished()) {
            return kubernetesClient.pods()
                    .inNamespace(props.getString("executor.env.namespace"))
                    .withName(KUN_WORKFLOW + taskAttemptId)
                    .tailingLines(tailLines)
                    .getLog();
        }
        logger.debug("pod with taskAttemptId = {} is not running", taskAttemptId);
        return null;
    }


    @Override
    public List<WorkerInstance> getRunningWorker() {
        List<PodStatusSnapShot> podList = getExistPodList();
        List<WorkerInstance> runningWorkers = new ArrayList<>();
        podList.forEach(x -> {
            if (x.getStatus().isFinished()) {
                stopWorker(x.getIns().getTaskAttemptId());
            } else {
                runningWorkers.add(x.getIns());
            }
        });
        return runningWorkers;
    }

    public List<PodStatusSnapShot> getExistPodList() {
        PodList podList = kubernetesClient.pods()
                .inNamespace(props.getString("executor.env.namespace"))
                .withLabel(KUN_WORKFLOW)
                .list();
        return podList.getItems().stream().map(x ->
                PodStatusSnapShot.fromPod(x)).collect(Collectors.toList());
    }


    private Pod buildPod(TaskAttempt taskAttempt) {
        Pod pod = new Pod();
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(KUN_WORKFLOW + taskAttempt.getId());
        objectMeta.setNamespace(props.getString("executor.env.namespace"));
        Map<String, String> labels = new HashMap<>();
        labels.put(KUN_WORKFLOW, null);
        labels.put(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttempt.getId()));
        labels.put(TASK_QUEUE, taskAttempt.getQueueName());
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
        String imageName = POD_IMAGE_NAME;
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
        jarMount.setMountPath(POD_LIB_DIR);
        jarMount.setName(props.getString("executor.env.nfsName"));
        jarMount.setSubPath(props.getString("executor.env.jarDirectory"));
        ExecCommand command = buildExecCommand(taskAttempt);
        writeExecCommandToPVC(command);
        mounts.add(jarMount);
        container.setVolumeMounts(mounts);
        container.setEnv(buildEnv(taskAttempt));
        return container;
    }

    private List<EnvVar> buildEnv(TaskAttempt taskAttempt) {
        logger.debug("building pod env,taskAttemptId = {}", taskAttempt.getId());
        List<EnvVar> envVarList = new ArrayList<>();
        addVar(envVarList, "execCommandFile", POD_LIB_DIR + "/" + taskAttempt.getId());
        configDBEnv(envVarList);
        return envVarList;
    }

    private ExecCommand buildExecCommand(TaskAttempt taskAttempt) {
        Operator operatorDetail = operatorDao.fetchById(taskAttempt.getTaskRun().getTask().getOperatorId())
                .orElseThrow(EntityNotFoundException::new);
        ExecCommand command = new ExecCommand();
        command.setTaskAttemptId(taskAttempt.getId());
        command.setTaskRunId(taskAttempt.getTaskRun().getId());
        command.setConfig(taskAttempt.getTaskRun().getConfig());
        command.setLogPath(taskAttempt.getLogPath());
        command.setJarPath(operatorDetail.getPackagePath());
        command.setClassName(operatorDetail.getClassName());
        command.setQueueName(taskAttempt.getQueueName());
        logger.debug("Execute task. attemptId={}, command={}", taskAttempt.getId(), command);

        return command;
    }

    private void writeExecCommandToPVC(ExecCommand execCommand) {
        String filePath = POD_LIB_DIR + "/" + execCommand.getTaskAttemptId();
        try {
            File execCommandFile = new File(filePath);
            JsonCodec.MAPPER.writeValue(execCommandFile, execCommand);
        } catch (IOException e) {
            logger.error("failed to write exec command to file = {} ", filePath, e);
        }
    }

    private void configDBEnv(List<EnvVar> envVarList) {
        addVar(envVarList, "datasource.jdbcUrl", props.get("datasource.jdbcUrl"));
        addVar(envVarList, "datasource.username", props.get("datasource.username"));
        addVar(envVarList, "datasource.password", props.get("datasource.password"));
        addVar(envVarList, "datasource.driverClassName", props.get("datasource.driverClassName"));
        addVar(envVarList, "datasource.maxPoolSize", DB_MAX_POOL.toString());
        addVar(envVarList, "datasource.minimumIdle", MINI_MUM_IDLE.toString());
        addVar(envVarList, "neo4j.uri", props.get("neo4j.uri"));
        addVar(envVarList, "neo4j.username", props.get("neo4j.username"));
        addVar(envVarList, "neo4j.password", props.get("neo4j.password"));
    }

    private void addVar(List<EnvVar> envVarList, String name, String value) {
        EnvVar envVar = new EnvVar();
        envVar.setName(name);
        envVar.setValue(value);
        envVarList.add(envVar);
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
