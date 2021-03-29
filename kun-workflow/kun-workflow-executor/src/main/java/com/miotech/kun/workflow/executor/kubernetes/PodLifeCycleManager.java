package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.workerInstance.WorkerInstanceDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.local.MiscService;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_TASK_ATTEMPT_ID;
import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_WORKFLOW;

public class PodLifeCycleManager extends WorkerLifeCycleManager {

    private final KubernetesClient kubernetesClient;
    private final OperatorDao operatorDao;

    @Inject
    public PodLifeCycleManager(TaskRunDao taskRunDao, WorkerInstanceDao workerInstanceDao,
                               WorkerMonitor workerMonitor, Props props, MiscService miscService,
                               KubernetesClient kubernetesClient, OperatorDao operatorDao) {
        super(taskRunDao, workerInstanceDao, workerMonitor, props, miscService);
        this.kubernetesClient = kubernetesClient;
        this.operatorDao = operatorDao;
    }

    @Override
    public WorkerSnapshot startWorker(TaskAttempt taskAttempt) {
        Pod pod = kubernetesClient.pods().create(buildPod(taskAttempt));
        return PodStatusSnapShot.fromPod(pod);
    }

    @Override
    public Boolean stopWorker(WorkerInstance workerInstance) {
        return kubernetesClient.pods()
                .withLabel(KUN_WORKFLOW)
                .withLabel(KUN_TASK_ATTEMPT_ID, String.valueOf(workerInstance.getTaskAttemptId()))
                .delete();
    }

    @Override
    public WorkerSnapshot getWorker(TaskAttempt taskAttempt) {
        PodList podList = kubernetesClient.pods()
                .withLabel(KUN_WORKFLOW)
                .withLabel(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttempt.getId()))
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
        pod.setApiVersion(props.get("executor.env.version"));
        pod.setKind("Job");
        ObjectMeta objectMeta = new ObjectMeta();
        Map<String, String> labels = new HashMap<>();
        labels.put(KUN_WORKFLOW, null);
        labels.put(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttempt.getId()));
        objectMeta.setLabels(labels);
        pod.setMetadata(objectMeta);
        pod.setSpec(buildSpec(taskAttempt));
        return pod;
    }

    private PodSpec buildSpec(TaskAttempt taskAttempt) {
        taskAttempt.getTaskRun().getTask().getOperatorId();
        PodSpec podSpec = new PodSpec();
        podSpec.setRestartPolicy("Never");
        podSpec.setContainers(Arrays.asList(buildContainer(taskAttempt)));
        return podSpec;
    }

    private Container buildContainer(TaskAttempt taskAttempt) {
        Long operatorId = taskAttempt.getTaskRun().getTask().getOperatorId();
        Container container = new Container();
        container.setImage(getContainerFromOperator(operatorId));
        container.setCommand(buildCommand(taskAttempt.getId()));
        List<VolumeMount> mounts = new ArrayList<>();
        String logPath = props.getString("executor.env.logPath");
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setMountPath(logPath);
        volumeMount.setName("logPath");
        container.setVolumeMounts(mounts);
        container.setEnv(buildEnv(taskAttempt));
        return container;
    }

    private List<EnvVar> buildEnv(TaskAttempt taskAttempt) {
        List<EnvVar> envVarList = new ArrayList<>();
        EnvVar logVar = new EnvVar();
        logVar.setName("logPath");
        logVar.setValue(taskAttempt.getLogPath());
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
        return envVarList;
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
        return operatorDao.fetchById(operatorId).get().getName();
    }

    private List<String> buildCommand(Long taskAttemptId) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.addAll(buildJVMArgs(taskAttemptId));
        command.add("-classpath");
        command.add(buildClassPath());
        command.add("com.miotech.kun.workflow.worker.kubernetes.OperatorLauncher");
        return command;
    }

    private String buildClassPath() {
        String classPath = System.getProperty("java.class.path");
        checkState(StringUtils.isNotEmpty(classPath), "launcher jar should exist.");
        return classPath;
    }

    private List<String> buildJVMArgs(Long taskAttemptId) {
        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-XX:+PrintGCDetails");
        jvmArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
        jvmArgs.add(String.format("-XX:HeapDumpPath=/tmp/%d/heapdump.hprof", taskAttemptId));
        return jvmArgs;
    }
}
