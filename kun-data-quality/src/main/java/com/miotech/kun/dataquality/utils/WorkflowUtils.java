package com.miotech.kun.dataquality.utils;

import com.miotech.kun.dataquality.DataQualityConfiguration;
import com.miotech.kun.workflow.core.execution.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Component
public class WorkflowUtils {

    @Value("${metadata.datasource.url}")
    String metadataUrl;

    @Value("${metadata.datasource.username}")
    String metadataUsername;

    @Value("${metadata.datasource.password}")
    String metadataPassword;

    @Value("${metadata.datasource.driver-class-name}")
    String metadataDriverClass;

    public Config getTaskConfig(Long caseId) {
        return Config.newBuilder()
                .addConfig(DataQualityConfiguration.METADATA_DATASOURCE_URL, metadataUrl)
                .addConfig(DataQualityConfiguration.METADATA_DATASOURCE_USERNAME, metadataUsername)
                .addConfig(DataQualityConfiguration.METADATA_DATASOURCE_PASSWORD, metadataPassword)
                .addConfig(DataQualityConfiguration.METADATA_DATASOURCE_DIRVER_CLASS, metadataDriverClass)
                .addConfig("caseId", caseId)
                .build();
    }
}
