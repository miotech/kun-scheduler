package com.miotech.kun.dataquality.web.utils;

import com.miotech.kun.dataquality.web.model.bo.ValidateSqlRequest;
import com.miotech.kun.dataquality.web.model.entity.DataQualityRule;
import com.miotech.kun.dataquality.web.model.entity.DatasetBasic;
import com.miotech.kun.dataquality.web.model.entity.SQLParseResult;
import com.miotech.kun.dataquality.web.model.entity.ValidateSqlResult;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SQLValidator {

    @Autowired
    DatasetRepository datasetRepository;

    public ValidateSqlResult validate(SQLParseResult sqlParseResult, ValidateSqlRequest validateSqlRequest) {
        // check table name
        boolean tableNameIsMatched = validateTableName(sqlParseResult, validateSqlRequest);
        if (!tableNameIsMatched) {
            return ValidateSqlResult.failed("Not related to current dataset.");
        }

        // check column name
        List<String> parsedColumnNames = validateSqlRequest.getValidateRules().stream().map(DataQualityRule::getField).collect(Collectors.toList());
        boolean columnNamesIsMatched = validateColumnNames(parsedColumnNames, sqlParseResult.getColumnNames());
        if (!columnNamesIsMatched) {
            return ValidateSqlResult.failed("The column names returned in the SQL statement are inconsistent with the validation rules.");
        }

        return ValidateSqlResult.success();
    }

    public boolean validateTableName(SQLParseResult sqlParseResult, ValidateSqlRequest validateSqlRequest) {
        DatasetBasic selectedDataset = datasetRepository.findBasic(validateSqlRequest.getDatasetId());
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
