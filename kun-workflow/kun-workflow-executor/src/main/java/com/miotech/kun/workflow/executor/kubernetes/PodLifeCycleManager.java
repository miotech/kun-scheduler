package com.miotech.kun.workflow.executor.kubernetes;

import com.google.common.io.Files;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.FtlUtils;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.worker.filter.WorkerImageFilter;
import com.miotech.kun.workflow.common.worker.service.WorkerImageService;
import com.miotech.kun.workflow.core.StorageManager;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerImage;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.WorkerLifeCycleManager;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.*;

public class PodLifeCycleManager extends WorkerLifeCycleManager {

    private final Logger logger = LoggerFactory.getLogger(PodLifeCycleManager.class);
    private final KubernetesClient kubernetesClient;
    @Inject
    private OperatorDao operatorDao;
    @Inject
    private WorkerImageService workerImageService;
    private final StorageManager storageManager;
    private final String POD_WORK_DIR = "/server/target";
    private final String POD_LIB_DIR = "/server/lib";
    private final Integer DB_MAX_POOL = 1;
    private final Integer MINI_MUM_IDLE = 0;
    private final KubeConfig kubeConfig;
    private final ImageHub imageHub;
    private final String TEMPLATE_FILE = "pod.ftl";

    public PodLifeCycleManager(KubeExecutorConfig kubeExecutorConfig, PodEventMonitor workerMonitor,
                               KubernetesClient kubernetesClient, KubernetesResourceManager KubernetesResourceManager,
                               String name, StorageManager storageManager) {
        super(kubeExecutorConfig, workerMonitor, KubernetesResourceManager, name);
        this.kubernetesClient = kubernetesClient;
        this.storageManager = storageManager;
        this.kubeConfig = kubeExecutorConfig.getKubeConfig();
        this.imageHub = kubeExecutorConfig.getPrivateHub();
        logger.info("k8s pod life cycle manager: {} initialize", name);
    }

    @Override
    public void init() {
        super.init();
        //set active image to the latest one;
        WorkerImageFilter imageFilter = WorkerImageFilter.newBuilder()
                .withName(POD_IMAGE_NAME)
                .withPage(0)
                .withPageSize(1)
                .build();
        List<WorkerImage> workerImageList = workerImageService.fetchWorkerImage(imageFilter).getRecords();
        if (workerImageList.size() > 0) {
            WorkerImage workerImage = workerImageList.get(0);
            workerImageService.setActiveVersion(workerImage.getId(), workerImage.getImageName());
        }
    }

    @Override
    protected void injectMembers(Injector injector) {
        injector.injectMembers(this);
    }

    @Override
    public void startWorker(TaskAttempt taskAttempt) {
        //logger.info("going to start pod taskAttemptId = {}", taskAttempt.getId());
        logger.info("pod life cycle manager: {} start worker pod taskAttemptId = {}", name, taskAttempt.getId());
        Map<String, Object> podConfig = buildPodConfig(taskAttempt);
        String podFile = "pod" + taskAttempt.getId() + ".yaml";
        logger.debug("write pod config {} to {}", podConfig, podFile);
        writeYaml(podConfig, podFile);
        logger.debug("pod file is {}", Files.asCharSource(new File(podFile), Charset.defaultCharset()));
        try {
            Pod pod = kubernetesClient.pods().load(new FileInputStream(podFile)).get();
            kubernetesClient.pods()
                    .inNamespace(kubeConfig.getNamespace())
                    .create(pod);
        } catch (IOException e) {
            logger.error("load pod from {} failed", podFile, e);
        }
    }

    @Override
    public Boolean stopWorker(Long taskAttemptId) {
        logger.info("going to stop pod taskAttemptId = {}", taskAttemptId);
        String podFile = "pod" + taskAttemptId + ".yaml";
        cleanPodFile(podFile);
        return kubernetesClient.pods()
                .inNamespace(kubeConfig.getNamespace())
                .withLabel(KUN_WORKFLOW)
                .withLabel(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttemptId))
                .delete();
    }

    @Override
    public WorkerSnapshot getWorker(Long taskAttemptId) {
        logger.info("pod life cycle manager: {} get worker", name);
        PodList podList = kubernetesClient.pods()
                .inNamespace(kubeConfig.getNamespace())
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
                    .inNamespace(kubeConfig.getNamespace())
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
            if (x.getStatus().isFinished() || x.getStatus().isChecking()) {
                stopWorker(x.getIns().getTaskAttemptId());
            } else {
                runningWorkers.add(x.getIns());
            }
        });
        return runningWorkers;
    }

    public List<PodStatusSnapShot> getExistPodList() {
        PodList podList = kubernetesClient.pods()
                .inNamespace(kubeConfig.getNamespace())
                .withLabel(KUN_WORKFLOW)
                .list();
        return podList.getItems().stream().map(x ->
                PodStatusSnapShot.fromPod(x)).collect(Collectors.toList());
    }

    private Map<String, Object> buildPodConfig(TaskAttempt taskAttempt) {
        Map<String, Object> podConfig = new HashMap<>();
        //set labales
        podConfig.put(KUN_WORKFLOW, null);
        podConfig.put(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttempt.getId()));
        podConfig.put(TASK_QUEUE, taskAttempt.getQueueName());

        //set meta info
        podConfig.put("podName", KUN_WORKFLOW + taskAttempt.getId());
        podConfig.put("namespace", kubeConfig.getNamespace());

        //set secret
        if (imageHub != null) {
            if (imageHub.getUseSecret()) {
                LocalObjectReference secret = new LocalObjectReferenceBuilder()
                        .withName(imageHub.getSecret())
                        .build();
                podConfig.put("dockerSecret", imageHub.getSecret());
            }
        }

        //set volume
        podConfig.put("pvName", kubeConfig.getNfsName());
        podConfig.put("subPath", kubeConfig.getNfsClaimName());

        //set container
        Long operatorId = taskAttempt.getTaskRun().getTask().getOperatorId();
        String containerName = getContainerFromOperator(operatorId);
        WorkerImage workerImage = workerImageService.fetchActiveImage(POD_IMAGE_NAME);
        String imageName = POD_IMAGE_NAME + ":" + workerImage.getVersion();
        if (imageHub != null) {
            imageName = imageHub.getUrl() + "/" + imageName;
        }
        podConfig.put("containerName", containerName);
        podConfig.put("imageName", imageName);
        podConfig.put("pullPolicy", IMAGE_PULL_POLICY);

        //set command
        podConfig.put("commandList", buildCommand(taskAttempt.getId()));

        //mount
        List<VolumeMount> mounts = new ArrayList<>();
        VolumeMount logMount = new VolumeMount();
        logMount.setMountPath(POD_WORK_DIR + "/logs");
        logMount.setName(kubeConfig.getNfsName());
        logMount.setSubPath(kubeConfig.getLogPath());
        mounts.add(logMount);
        VolumeMount jarMount = new VolumeMount();
        jarMount.setMountPath(POD_LIB_DIR);
        jarMount.setName(kubeConfig.getNfsName());
        jarMount.setSubPath(kubeConfig.getJarDirectory());
        podConfig.put("volumeList", mounts);

        ExecCommand command = buildExecCommand(taskAttempt);
        writeExecCommandToPVC(command);
        mounts.add(jarMount);
        //set env
        podConfig.put("envList", buildEnv(taskAttempt));

        return podConfig;
    }

    public void writeYaml(Map<String, Object> podConfig, String output) {
        FtlUtils.processTemplate(this.getClass(), TEMPLATE_FILE, output, podConfig);
    }

    private List<EnvVar> buildEnv(TaskAttempt taskAttempt) {
        logger.debug("building pod env,taskAttemptId = {}", taskAttempt.getId());
        List<EnvVar> envVarList = new ArrayList<>();
        addVar(envVarList, "execCommandFile", POD_LIB_DIR + "/" + taskAttempt.getId());
        //set rpc config
        String rpcHost = System.getenv().get("KUN_INFRA_HOST");
        if(Strings.isNullOrEmpty(rpcHost)){
            rpcHost = "kun-infra";
        }
        addVar(envVarList,"executorRpcHost",rpcHost);
        addVar(envVarList,"executorRpcPort","10201");
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
        command.setExecuteTarget(taskAttempt.getTaskRun().getExecuteTarget());
        logger.debug("Execute task. attemptId={}, command={}", taskAttempt.getId(), command);

        return command;
    }

    private void writeExecCommandToPVC(ExecCommand execCommand) {
        storageManager.writeExecCommand(execCommand);
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
        envVar.setKey(name);
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

    private void cleanPodFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                logger.error("can not delete pod {} file", fileName, e);
            }
        }
    }
}
