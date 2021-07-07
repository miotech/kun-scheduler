package com.miotech.kun.workflow.operator;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.JSONUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.*;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.DockerConfiguration.*;

public class KubernetesOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesOperator.class);
    private static final String POD_READY = "Ready";
    private static final String STATUS_TRUE = "True";

    private KubernetesClient k8sClient;
    private Job job;
    private Map<String, Long> logMap = new ConcurrentHashMap<>();

    /**
     * setup KubernetesClient for test usage
     * @param client: KubernetesClient
     */
    public void setClient(KubernetesClient client) {
        k8sClient = client;
    }

    @Override
    public boolean run() {
        k8sClient = initClient();

        Config config = getContext().getConfig();
        String namespace = config.getString(CONF_K8S_POD_NAMESPACE);
        String image = config.getString(CONF_CONTAINER_IMAGE);
        String command = resolveCommand(config);
        String name = config.getString(CONF_CONTAINER_NAME)
                .replaceAll("\\s+", "");
        String jobName = config.getString(CONF_K8S_JOB_NAME);
        if (StringUtils.isBlank(jobName)) {
            jobName = "k8s-job-" + IdGenerator.getInstance().nextId();
        }

        logger.info("Execute kubernetes job {} : image - {}, container - {} , command - \"{}\" ",
                jobName,
                image,
                name,
                command);

        // Initialize podSpec
        PodSpecBuilder podSpecBuilder = new PodSpecBuilder()
                .withRestartPolicy("Never");
        // imagePullSecret
        String imagePullSecret = config.getString(CONF_K8S_POD_IMAGEPULLSECRET);
        if (StringUtils.isNoneEmpty(imagePullSecret)) {
            podSpecBuilder.withImagePullSecrets(
                    new LocalObjectReferenceBuilder()
                            .withName(imagePullSecret)
                            .build());
        }

        // Prepare Container
        // commands
        List<String> commands = new ArrayList<>(Arrays.asList(command.split("\\s+")));

        // environments
        String envJson = config.getString(CONF_CONTAINER_ENV);
        Map<String, String> containerEnv = JSONUtils.jsonStringToStringMap(envJson);
        List<EnvVar> envList = containerEnv.entrySet()
                .stream()
                .map(x -> new EnvVarBuilder()
                        .withName(x.getKey())
                        .withValue(x.getValue())
                        .build()
                ).collect(Collectors.toList());

        ContainerBuilder containerBuilder = new ContainerBuilder()
                .withCommand(commands)
                .withImage(image)
                .withEnv(envList)
                .withName(name);

        // Mount config
        mountConfigWithConfigMap(namespace, jobName, containerBuilder, podSpecBuilder);

        // labels
        Map<String, String> labels = ImmutableMap
                .of(LABEL_PROJECT, LABEL_PROJECT_VALUE,
                        LABEL_OPERATOR, LABEL_OPERATOR_VALUE,
                        LABEL_JOB_ID, jobName
                );

        // submit job
        podSpecBuilder.withContainers(containerBuilder.build());
        PodTemplateSpec podTemplateSpec = createPodSpec(namespace, jobName, podSpecBuilder.build(), labels);
        job = createJob(namespace, jobName, labels, podTemplateSpec);
        logger.debug("Submit job: {}", JSONUtils.toJsonString(job));
        k8sClient.batch()
                .jobs()
                .inNamespace(namespace)
                .create(job);

        // tailing job
        TaskRunStatus status;
        do {
            try {
                status = tailingJob(namespace, jobName, labels);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error("", e);
                Thread.currentThread().interrupt();
                status = TaskRunStatus.FAILED;
            } catch (Exception e) {
                logger.error("", e);
                status = TaskRunStatus.FAILED;
            }
        } while(!status.isFinished());

        cleanup(namespace, jobName);
        return status.isSuccess();
    }

    @Override
    public void abort() {
        if (job != null) {
            ObjectMeta meta = job.getMetadata();
            cleanup(meta.getNamespace(), meta.getName());
        }
    }

    private void cleanup(String namespace, String jobName) {
        try {
            logger.info("Cleanup job resource : namespace - {}, job - {}", namespace, jobName);
            k8sClient.configMaps()
                    .inNamespace(namespace)
                    .withName(buildConfigMapName(jobName))
                    .delete();

            k8sClient.batch().jobs()
                    .inNamespace(namespace)
                    .withName(jobName)
                    .delete();
        } catch (Exception e) {
            logger.error("Failed to terminate job: namespace - {}, job - {}", namespace, jobName);
        }
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(CONF_CONTAINER_IMAGE, ConfigDef.Type.STRING, true, CONF_CONTAINER_IMAGE_DOC, CONF_CONTAINER_IMAGE_DISPLAY)
                .define(CONF_CONTAINER_COMMAND, ConfigDef.Type.STRING, true, CONF_CONTAINER_COMMAND_DOC, CONF_CONTAINER_COMMAND_DISPLAY)
                .define(CONF_CONTAINER_NAME, ConfigDef.Type.STRING, true, CONF_CONTAINER_NAME_DOC, CONF_CONTAINER_NAME_DISPLAY)
                .define(CONF_CONTAINER_ENV, ConfigDef.Type.STRING, CONF_CONTAINER_ENV_DEFAULT,true, CONF_CONTAINER_ENV_DOC, CONF_CONTAINER_ENV_DISPLAY)
                .define(CONF_VARIABLES, ConfigDef.Type.STRING, CONF_VARIABLES_DEFAULT,true, CONF_VARIABLES_DOC, CONF_VARIABLES_DISPLAY)
                .define(CONF_K8S_API_HOST, ConfigDef.Type.STRING,true, CONF_K8S_API_HOST_DOC, CONF_K8S_API_HOST_DISPLAY)
                .define(CONF_K8S_API_USER, ConfigDef.Type.STRING, CONF_K8S_API_USER_DEFAULT,true, CONF_K8S_API_USER_DOC, CONF_K8S_API_USER_DISPLAY)
                .define(CONF_K8S_API_PASSWORD, ConfigDef.Type.STRING, CONF_K8S_API_PASSWORD_DEFAULT,true, CONF_K8S_API_PASSWORD_DOC, CONF_K8S_API_PASSWORD_DISPLAY)
                .define(CONF_K8S_API_TOKEN, ConfigDef.Type.STRING, CONF_K8S_API_TOKEN_DEFAULT,true, CONF_K8S_API_TOKEN_DOC, CONF_K8S_API_TOKEN_DISPLAY)
                .define(CONF_K8S_CA_CERTDIR, ConfigDef.Type.STRING, CONF_K8S_CA_CERTDIR_DEFAULT,true, CONF_K8S_CA_CERTDIR_DOC, CONF_K8S_CA_CERTDIR_DISPLAY)
                .define(CONF_K8S_POD_NAMESPACE, ConfigDef.Type.STRING, CONF_K8S_POD_NAMESPACE_DEFAULT, true, CONF_K8S_POD_NAMESPACE_DOC, CONF_K8S_POD_NAMESPACE_DISPLAY)
                .define(CONF_K8S_POD_IMAGEPULLSECRET, ConfigDef.Type.STRING, "",true,CONF_K8S_POD_IMAGEPULLSECRET_DOC, CONF_K8S_POD_IMAGEPULLSECRET_DISPLAY)
                .define(CONF_K8S_POD_CONFIG_MNT, ConfigDef.Type.STRING, CONF_K8S_POD_CONFIG_MNT_DEFAULT, true, CONF_K8S_POD_CONFIG_MNT_DOC, CONF_K8S_POD_CONFIG_MNT_DISPLAY)
                .define(CONF_K8S_JOB_NAME, ConfigDef.Type.STRING, CONF_K8S_JOB_NAME_DEFAULT, true, CONF_K8S_JOB_NAME_DOC, CONF_K8S_JOB_NAME_DOC_DISPLAY)
                ;
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }

    private PodTemplateSpec createPodSpec(String namespace,
                                          String name,
                                          PodSpec podSpec,
                                          Map<String,String> labels) {
        // pod labels is used for filtering
        ObjectMeta podMeta = new ObjectMetaBuilder()
                .withName(name)
                .withNamespace(namespace)
                .withLabels(labels)
                .build();
        return new PodTemplateSpecBuilder()
                .withMetadata(podMeta)
                .withSpec(podSpec)
                .build();
    }

    private Job createJob(String namespace,
                          String name,
                          Map<String,String> labels,
                          PodTemplateSpec podTemplateSpec) {
        JobSpec spec = new JobSpecBuilder()
                .withBackoffLimit(0)
                .withTemplate(podTemplateSpec)
                .build();

        ObjectMeta meta = new ObjectMetaBuilder()
                .withName(name)
                .withNamespace(namespace)
                .withLabels(labels)
                .build();

        return new JobBuilder()
                .withMetadata(meta)
                .withSpec(spec)
                .build();
    }

    /**
     * Create a shared volume using ConfigMap
     * @param namespace
     * @param containerBuilder
     * @param podSpecBuilder
     */
    private void mountConfigWithConfigMap(String namespace, String jobName, ContainerBuilder containerBuilder, PodSpecBuilder podSpecBuilder) {
        Config config = getContext().getConfig();
        HashMap<String, String> data = new HashMap<>();
        data.put("config.json", JSONUtils.toJsonString(config.getValues()));

        String configMapName = buildConfigMapName(jobName);
        Map<String, String> configMapLabels = ImmutableMap.of(
                LABEL_CONFIG_MAP_ID, configMapName,
                LABEL_PROJECT, LABEL_PROJECT_VALUE
        );

        ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(configMapName)
                .withLabels(configMapLabels)
                .endMetadata()
                .withData(data)
                .build();
        logger.debug("Create configMap: {}", configMap);
        k8sClient.configMaps().inNamespace(namespace).create(configMap);

        String volumeName = "kun-config-volume";
        Volume volume = new VolumeBuilder()
                .withName(volumeName)
                .withNewConfigMap()
                .withName(configMapName)
                .endConfigMap()
                .build();
        String mountPoint = config.getString(CONF_K8S_POD_CONFIG_MNT);
        EnvVar volumeEnv = new EnvVarBuilder()
                .withName(CONF_K8S_POD_CONFIG_MNT_ENV)
                .withValue(mountPoint)
                .build();
        logger.debug("Add env {} and share volume: {}", volumeEnv, volume);
        // only contains the config part
        containerBuilder
                .withEnv(volumeEnv)
                .addNewVolumeMount()
                .withName(volumeName)
                .withMountPath(mountPoint)
                .endVolumeMount();
        podSpecBuilder.withVolumes(volume);
    }

    private TaskRunStatus tailingJob(String namespace, String jobName, Map<String, String> labels) {
        Job remote = k8sClient.batch().jobs()
                .inNamespace(namespace)
                .withName(jobName)
                .get();
        JobStatus status = remote.getStatus();
        TaskRunStatus currentState = TaskRunStatus.CREATED;

        Optional<JobCondition> failedCondition = status.getConditions()
                .stream()
                .filter ( t ->t.getType().equals("Failed"))
                .findFirst();

        // Job Failed in Initialization
        if (failedCondition.isPresent()) {
            logger.error(failedCondition.get().getReason());
            currentState = TaskRunStatus.FAILED;
        } else {
            boolean isCompleted = StringUtils.isNoneBlank(status.getCompletionTime());
            if (!isCompleted) {
                currentState = TaskRunStatus.RUNNING;
            } else if (status.getFailed() != null
                    && status.getFailed() > 0) {
                currentState = TaskRunStatus.FAILED;
            } else if (status.getSucceeded() != null
                    && status.getSucceeded() > 0) {
                currentState = TaskRunStatus.SUCCESS;
            }
        }

        // job pod status
        for (Pod pod : k8sClient.pods()
                .inNamespace(namespace)
                .withLabels(labels)
                .list().getItems()) {
            List<PodCondition> conditions = pod.getStatus().getConditions();
            boolean isReady = conditions
                    .stream()
                    .anyMatch(t -> t.getType().equals(POD_READY)
                            &&  t.getStatus().equals(STATUS_TRUE));
            // if pod is available
            if (isReady || currentState.isFinished()) {
                tailingLog(pod);
            } else {
                logger.debug("Pod - {} is not ready", pod.getMetadata().getName());
            }
        }
        return currentState;
    }

    private void tailingLog(Pod pod) {

        try {
            // fetch log from last footprint
            String podName = pod.getMetadata().getName();
            int from = logMap.getOrDefault(podName, 0L).intValue();
            PodResource<Pod> podResource = k8sClient
                    .pods()
                    .inNamespace(pod.getMetadata().getNamespace())
                    .withName(podName);
            String logs;

            if (from > 0) {
                logs = podResource
                        .usingTimestamps()
                        .sinceSeconds(from)
                        .getLog();
            } else {
                logs = podResource
                        .usingTimestamps()
                        .getLog();
            }
            logger.info(logs);
            logMap.put(podName, System.currentTimeMillis()/1000);
        } catch (KubernetesClientException e) {
            logger.error("Failed to fetch pod log, current pod status: {} ", pod.toString(), e);
        }
    }

    private String buildConfigMapName(String jobName) {
        return "configmap-" + jobName;
    }

    private KubernetesClient initClient() {
        if (k8sClient != null) return k8sClient;

        Config config = getContext().getConfig();
        String apiHost = config.getString(CONF_K8S_API_HOST);
        String apiUser = config.getString(CONF_K8S_API_USER);
        String apiPass = config.getString(CONF_K8S_API_PASSWORD);
        String apiToken = config.getString(CONF_K8S_API_TOKEN);
        String podNamespace = config.getString(CONF_K8S_POD_NAMESPACE);
        String caCertDir= config.getString(CONF_K8S_CA_CERTDIR);

        ConfigBuilder builder = new ConfigBuilder()
                .withMasterUrl(apiHost);
        if (StringUtils.isNoneEmpty(apiUser)) {
            builder.withProxyUsername(apiUser);
            builder.withUsername(apiUser);
        }
        if (StringUtils.isNoneEmpty(apiPass)) {
            builder.withPassword(apiPass);
        }
        if (StringUtils.isNoneEmpty(apiToken)) {
            builder.withOauthToken(apiToken);
        }
        if (StringUtils.isNoneEmpty(podNamespace)) {
            builder.withNamespace(podNamespace);
        }
        if (StringUtils.isNoneEmpty(caCertDir)) {
            builder.withCaCertFile(caCertDir);
        }
        return new DefaultKubernetesClient(builder.build());
    }
}
