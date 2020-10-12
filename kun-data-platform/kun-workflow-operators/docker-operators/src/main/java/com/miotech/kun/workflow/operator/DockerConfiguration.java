package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.Map;

public class DockerConfiguration {

    private DockerConfiguration() {}

    // basic container config
    public static final String CONF_CONTAINER_IMAGE = "container.image";
    public static final String CONF_CONTAINER_IMAGE_DISPLAY = "image";
    public static final String CONF_CONTAINER_IMAGE_DOC = "container image name";

    public static final String CONF_CONTAINER_NAME = "container.name";
    public static final String CONF_CONTAINER_NAME_DISPLAY = "name";
    public static final String CONF_CONTAINER_NAME_DOC = "container name";

    public static final String CONF_CONTAINER_COMMAND = "container.command";
    public static final String CONF_CONTAINER_COMMAND_DISPLAY = "command";
    public static final String CONF_CONTAINER_COMMAND_DOC = "container command, must start with full path: /bin/sh xx.sh.";

    public static final String CONF_CONTAINER_ENV = "container.environment";
    public static final String CONF_CONTAINER_ENV_DEFAULT = "{}";
    public static final String CONF_CONTAINER_ENV_DISPLAY = "environment";
    public static final String CONF_CONTAINER_ENV_DOC = "container environment variables. Should specify as {\"env1\": \"value1\"}";

    public static final String CONF_VARIABLES = "variables";
    public static final String CONF_VARIABLES_DEFAULT = "{}";
    public static final String CONF_VARIABLES_DISPLAY = "environment";
    public static final String CONF_VARIABLES_DOC = "variables, support in \"container.command\". For example, \"container.command\" is \"echo {{ name }}\". The variable should specify as {\"name\": \"hell world\"}";

    // kubernetes
    public static final String CONF_K8S_API_HOST = "k8s.api.host";
    public static final String CONF_K8S_API_HOST_DOC = "Kubernetes api serverHost";
    public static final String CONF_K8S_API_HOST_DISPLAY = "apiServerHost";

    public static final String CONF_K8S_API_USER = "k8s.api.user";
    public static final String CONF_K8S_API_USER_DEFAULT = "";
    public static final String CONF_K8S_API_USER_DOC = "Kubernetes api user name";
    public static final String CONF_K8S_API_USER_DISPLAY = "apiUser";

    public static final String CONF_K8S_API_PASSWORD = "k8s.api.password";
    public static final String CONF_K8S_API_PASSWORD_DEFAULT = "";
    public static final String CONF_K8S_API_PASSWORD_DOC = "Kubernetes api password";
    public static final String CONF_K8S_API_PASSWORD_DISPLAY = "apiPassword";

    public static final String CONF_K8S_API_TOKEN = "k8s.api.token";
    public static final String CONF_K8S_API_TOKEN_DEFAULT = "";
    public static final String CONF_K8S_API_TOKEN_DOC = "Kubernetes api oauth token";
    public static final String CONF_K8S_API_TOKEN_DISPLAY = "apiOauthToken";

    public static final String CONF_K8S_CA_CERTDIR = "k8s.ca.certDir";
    public static final String CONF_K8S_CA_CERTDIR_DEFAULT = "";
    public static final String CONF_K8S_CA_CERTDIR_DOC = "kubernetes ca certificate directory if ssl enabled";
    public static final String CONF_K8S_CA_CERTDIR_DISPLAY = "caCertDir";

    public static final String CONF_K8S_POD_IMAGEPULLSECRET = "k8s.pod.imagePullSecret";
    public static final String CONF_K8S_POD_IMAGEPULLSECRET_DOC = "Kubernetes pod imagepullsecret";
    public static final String CONF_K8S_POD_IMAGEPULLSECRET_DISPLAY = "podImagePullSecret";

    public static final String CONF_K8S_POD_NAMESPACE = "k8s.pod.namespace";
    public static final String CONF_K8S_POD_NAMESPACE_DEFAULT = "default";
    public static final String CONF_K8S_POD_NAMESPACE_DOC = "Kubernetes namespace to launch pod";
    public static final String CONF_K8S_POD_NAMESPACE_DISPLAY = "podNamespace";

    public static final String CONF_K8S_POD_CONFIG_MNT = "k8s.pod.configMount";
    public static final String CONF_K8S_POD_CONFIG_MNT_DEFAULT = "/opt/kun_wf/config";
    public static final String CONF_K8S_POD_CONFIG_MNT_DOC = "Kubernetes mount place to for configuration, default is \"/opt/kun_wf/config\"";
    public static final String CONF_K8S_POD_CONFIG_MNT_DISPLAY = "podConfigMount";
    public static final String CONF_K8S_POD_CONFIG_MNT_ENV = "KUN_CONFIG_MNT";

    public static final String CONF_K8S_JOB_NAME = "k8s.job.name";
    public static final String CONF_K8S_JOB_NAME_DEFAULT = "";
    public static final String CONF_K8S_JOB_NAME_DOC = "Kubernetes job name";
    public static final String CONF_K8S_JOB_NAME_DOC_DISPLAY = "jobName";

    public static final String LABEL_CONFIG_MAP_ID = "config-map-id";
    public static final String LABEL_PROJECT = "project";
    public static final String LABEL_PROJECT_VALUE = "kun-workflow";

    public static final String LABEL_OPERATOR = "operator";
    public static final String LABEL_OPERATOR_VALUE = "KubernetesOperator";

    public static final String LABEL_JOB_ID= "job-id";

    public static String resolveCommand(Config config) {
        String command = config.getString(CONF_CONTAINER_COMMAND);
        return resolveWithVariable(command, config);
    }

    public static String resolveWithVariable(String text, Config config) {
        Map<String, String> variables = JSONUtils.jsonStringToStringMap(config.getString(CONF_VARIABLES));
        return StringUtils.resolveWithVariable(text, variables);
    }

}
