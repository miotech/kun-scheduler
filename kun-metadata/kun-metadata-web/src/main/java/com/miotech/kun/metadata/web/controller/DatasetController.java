package com.miotech.kun.metadata.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestRequest;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestResponse;
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

    @RouteMapping(url = "/dataset/database/_suggest", method = "GET")
    public List<String> suggestDatabase(@QueryParameter String prefix) {
        logger.debug("Suggest database, prefix: {}", prefix);
        return datasetService.suggestDatabase(prefix);
    }

    @RouteMapping(url = "/dataset/table/_suggest", method = "GET")
    public List<String> suggestTable(@QueryParameter(required = true) String databaseName, @QueryParameter String prefix) {
        logger.debug("Suggest table, databaseName: {}, prefix: {}", databaseName, prefix);
        return datasetService.suggestTable(databaseName, prefix);
    }

    @RouteMapping(url = "/dataset/column/_suggest", method = "POST")
    public List<DatasetColumnSuggestResponse> suggestColumn(@RequestBody List<DatasetColumnSuggestRequest> columnSuggestRequests) {
        logger.debug("Suggest column, columnSuggestRequests: {}", columnSuggestRequests);
        String checkParamResult = check(columnSuggestRequests);
        if (StringUtils.isNotBlank(checkParamResult)) {
            throw new IllegalArgumentException(checkParamResult);
        }

        return datasetService.suggestColumn(columnSuggestRequests);
    }

    private String check(List<DatasetColumnSuggestRequest> columnSuggestRequests) {
        if (CollectionUtils.isEmpty(columnSuggestRequests)) {
            return "requests should not be empty";
        }

        for (DatasetColumnSuggestRequest columnSuggestRequest : columnSuggestRequests) {
            if (StringUtils.isBlank(columnSuggestRequest.getDatabaseName())) {
                return "databaseName should not be empty";
            }

            if (StringUtils.isBlank(columnSuggestRequest.getTableName())) {
                return "tableName should not be empty";
            }
        }

        return StringUtils.EMPTY;
    }

}
