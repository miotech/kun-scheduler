package com.miotech.kun.dataquality.utils;

import com.miotech.kun.dataquality.DataQualityConfiguration;
import com.miotech.kun.workflow.core.model.common.Param;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    public List<Param> getInitParams(String caseId) {
        List<Param> params = new ArrayList<>();
        params.add(Param.newBuilder()
                .withName(DataQualityConfiguration.METADATA_DATASOURCE_URL)
                .withValue(metadataUrl)
                .withDescription("")
                .build());
        params.add(Param.newBuilder()
                .withName(DataQualityConfiguration.METADATA_DATASOURCE_USERNAME)
                .withValue(metadataUsername)
                .withDescription("")
                .build());
        params.add(Param.newBuilder()
                .withName(DataQualityConfiguration.METADATA_DATASOURCE_PASSWORD)
                .withValue(metadataPassword)
                .withDescription("")
                .build());
        params.add(Param.newBuilder()
                .withName(DataQualityConfiguration.METADATA_DATASOURCE_DIRVER_CLASS)
                .withValue(metadataDriverClass)
                .withDescription("")
                .build());
        params.add(getCaseIdParam(caseId));
        return params;
    }

    public Param getCaseIdParam(String caseId) {
        return Param.newBuilder()
                .withName("caseId")
                .withValue(caseId)
                .withDescription("")
                .build();
    }
}
