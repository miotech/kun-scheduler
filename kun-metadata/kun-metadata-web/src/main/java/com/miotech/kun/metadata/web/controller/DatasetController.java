package com.miotech.kun.metadata.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.core.model.DatasetColumnHintRequest;
import com.miotech.kun.metadata.core.model.DatasetColumnHintResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class DatasetController {

    private static final Logger logger = LoggerFactory.getLogger(DatasetController.class);

    @Inject
    private MetadataDatasetService datasetService;

    @RouteMapping(url = "/dataset/database/_hint", method = "GET")
    public List<String> hint(@QueryParameter String prefix) {
        logger.debug("Hint database, prefix: {}", prefix);
        return datasetService.hintDatabase(prefix);
    }

    @RouteMapping(url = "/dataset/table/_hint", method = "GET")
    public List<String> hint(@QueryParameter(required = true) String databaseName, @QueryParameter String prefix) {
        logger.debug("Hint table, databaseName: {}, prefix: {}", databaseName, prefix);
        return datasetService.hintTable(databaseName, prefix);
    }

    @RouteMapping(url = "/dataset/column/_hint", method = "POST")
    public List<DatasetColumnHintResponse> hint(@RequestBody List<DatasetColumnHintRequest> columnHintRequests) {
        logger.debug("Hint column, columnHintRequests: {}", columnHintRequests);
        String checkParamResult = check(columnHintRequests);
        if (StringUtils.isNotBlank(checkParamResult)) {
            throw new IllegalArgumentException(checkParamResult);
        }

        return datasetService.hintColumn(columnHintRequests);
    }

    private String check(List<DatasetColumnHintRequest> columnHintRequests) {
        if (CollectionUtils.isEmpty(columnHintRequests)) {
            return "requests should not be empty";
        }

        for (DatasetColumnHintRequest columnHintRequest : columnHintRequests) {
            if (StringUtils.isBlank(columnHintRequest.getDatabaseName())) {
                return "databaseName should not be empty";
            }

            if (StringUtils.isBlank(columnHintRequest.getTableName())) {
                return "tableName should not be empty";
            }
        }

        return StringUtils.EMPTY;
    }

}
