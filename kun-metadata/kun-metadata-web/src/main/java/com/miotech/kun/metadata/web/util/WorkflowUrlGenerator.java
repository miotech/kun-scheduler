package com.miotech.kun.metadata.web.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.web.constant.WorkflowApiParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import static com.miotech.kun.metadata.web.constant.PropKey.WORKFLOW_URL;

@Singleton
public class WorkflowUrlGenerator {

    private static final String BASE_GET_URL = "%s/%s?%s";
    private static final String BASE_POST_URL = "%s/%s";
    private static final String ENC = "UTF-8";

    @Inject
    private Properties properties;

    public String generateSearchOperatorUrl(String operatorName) {
        try {
            return String.format(BASE_GET_URL, properties.getProperty(WORKFLOW_URL), WorkflowApiParam.OPERATORS,
                    URLEncoder.encode("name=" + operatorName, ENC));
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public String generateCreateOperatorUrl() {
        return String.format(BASE_POST_URL, properties.getProperty(WORKFLOW_URL), WorkflowApiParam.OPERATORS);
    }

    public String buildFetchStatusUrl(String id) {
        String baseUrl = "%s/taskruns/%s/status";
        return String.format(baseUrl, properties.getProperty(WORKFLOW_URL), id);
    }

    public String generateSearchTaskUrl() {
        try {
            return String.format(BASE_GET_URL, properties.getProperty(WORKFLOW_URL), WorkflowApiParam.TASKS,
                    URLEncoder.encode("name=" + WorkflowApiParam.TASK_NAME, ENC));
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public String generateCreateTaskUrl() {
        return String.format(BASE_POST_URL, properties.getProperty(WORKFLOW_URL), WorkflowApiParam.TASKS);
    }

    public String generateRunTaskUrl() {
        return String.format(BASE_POST_URL, properties.getProperty(WORKFLOW_URL), WorkflowApiParam.TASKS_RUN);
    }
}
