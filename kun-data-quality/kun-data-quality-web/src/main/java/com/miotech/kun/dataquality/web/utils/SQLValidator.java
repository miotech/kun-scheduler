package com.miotech.kun.dataquality.web.utils;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.dataquality.web.model.bo.MetricsRequest;
import com.miotech.kun.dataquality.web.model.bo.ValidateMetricsRequest;
import com.miotech.kun.dataquality.web.model.entity.DatasetBasic;
import com.miotech.kun.dataquality.web.model.entity.SQLParseResult;
import com.miotech.kun.dataquality.web.model.entity.ValidateMetricsResult;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SQLValidator {

    @Autowired
    DatasetRepository datasetRepository;

    public ValidateMetricsResult validate(SQLParseResult sqlParseResult, ValidateMetricsRequest validateMetricsRequest) {
        MetricsRequest metricsRequest = validateMetricsRequest.getMetricsRequest();
        // check table name
        boolean tableNameIsMatched = validateTableName(sqlParseResult, validateMetricsRequest);
        if (!tableNameIsMatched) {
            return ValidateMetricsResult.failed("Not related to current dataset.");
        }

        // check column name
        List<String> parsedColumnNames = ImmutableList.of(metricsRequest.getField());
        boolean columnNamesIsMatched = validateColumnNames(parsedColumnNames, sqlParseResult.getColumnNames());
        if (!columnNamesIsMatched) {
            return ValidateMetricsResult.failed("The column names returned in the SQL statement are inconsistent with the validation rules.");
        }

        return ValidateMetricsResult.success();
    }

    public boolean validateTableName(SQLParseResult sqlParseResult, ValidateMetricsRequest validateMetricsRequest) {
        MetricsRequest metricsRequest = validateMetricsRequest.getMetricsRequest();
        DatasetBasic selectedDataset = datasetRepository.findBasic(metricsRequest.getDatasetGid());
        boolean validateSelectedDataset = false;
        for (String relatedDatasetName : sqlParseResult.getRelatedDatasetNames()) {
            String[] tableArray = relatedDatasetName.split("\\.");
            String dbName = tableArray.length == 1 ? selectedDataset.getDatabase() : tableArray[0];
            dbName = dbName.replaceAll("\"", "");

            String tableName = tableArray[tableArray.length - 1]
                    .replaceAll("\"", "");

            if (dbName.equalsIgnoreCase(selectedDataset.getDatabase())
                    && tableName.equalsIgnoreCase(selectedDataset.getName())) {
                validateSelectedDataset = true;
            }
        }

        return validateSelectedDataset;
    }

    public boolean validateColumnNames(List<String> expected, List<String> received) {
        if (CollectionUtils.isEmpty(expected) || CollectionUtils.isEmpty(received)) {
            return false;
        }

        if (expected.size() != received.size()) {
            return false;
        }

        for (String field : expected) {
            if (!received.contains(field)) {
                return false;
            }
        }

        return true;
    }

}
