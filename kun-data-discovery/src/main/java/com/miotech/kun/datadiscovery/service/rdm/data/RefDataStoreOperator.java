package com.miotech.kun.datadiscovery.service.rdm.data;

import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.*;
import com.google.common.collect.ImmutableMap;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.service.rdm.RefDataOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-21 14:30
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class RefDataStoreOperator implements RefDataOperator {

    private final AWSGlue awsGlue;
    @Value("${rdm.database-name:default}")
    private String databaseName;

    @Override
    public void override(RefTableVersionInfo refTableVersionInfo) {
        if (exists(refTableVersionInfo)) {
            updateTable(refTableVersionInfo);
        } else {
            createTable(refTableVersionInfo);
        }
    }

    @Override
    public void deactivate(String tableName) {
        DeleteTableRequest deleteTableRequest = new DeleteTableRequest().withDatabaseName(databaseName).withName(tableName);
        DeleteTableResult deleteTableResult = awsGlue.deleteTable(deleteTableRequest);
        log.debug(" glue deleteTable Result:{}", deleteTableResult);

    }

    private boolean exists(RefTableVersionInfo refTableVersionInfo) {
        GetTableRequest tablesRequest = new GetTableRequest().withDatabaseName(databaseName).withName(refTableVersionInfo.getTableName());
        try {
            GetTableResult tableResult = awsGlue.getTable(tablesRequest);
            return true;
        } catch (EntityNotFoundException e) {
            log.debug("table is not found:{}", tablesRequest);
            return false;
        }

    }

    private void createTable(RefTableVersionInfo refTableVersionInfo) {
        CreateTableRequest createTableRequest = new CreateTableRequest();
        createTableRequest.setDatabaseName(databaseName);
//        createTableRequest.setCatalogId(refTableVersionInfo.getTableName());

        TableInput tableInput = new TableInput();
        tableInput.setName(refTableVersionInfo.getTableName());
        StorageDescriptor storageDescriptor = new StorageDescriptor();
        storageDescriptor.setInputFormat("org.apache.hadoop.mapred.TextInputFormat");
        storageDescriptor.setOutputFormat("org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat");
        storageDescriptor.setCompressed(false);
        storageDescriptor.setNumberOfBuckets(-1);
        SerDeInfo serDeInfo = new SerDeInfo().withSerializationLibrary("org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe");
        serDeInfo.setParameters(
                ImmutableMap.of(
                        "field.delim", ",",
                        "serialization.format", ","));
        storageDescriptor.setSerdeInfo(serDeInfo);
        storageDescriptor.setLocation(refTableVersionInfo.getDataPath());
        LinkedHashSet<RefColumn> refTableColumns = refTableVersionInfo.getRefTableColumns();
        if (CollectionUtils.isEmpty(refTableColumns)) {
            log.error("columns is not empty:version id :{}", refTableVersionInfo.getVersionId());
            throw new IllegalStateException(String.format("columns is not empty:version id :%s", refTableVersionInfo.getVersionId()));
        }
        List<Column> columnList = refTableColumns.stream().map(refColumn -> new Column().withName(refColumn.getName()).withType(refColumn.getColumnType())).collect(Collectors.toList());
        storageDescriptor.setColumns(columnList);
        tableInput.setStorageDescriptor(storageDescriptor);
        tableInput.setTableType("EXTERNAL_TABLE");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("EXTERNAL", "TRUE");
        paramMap.put("classification", "csv");
        paramMap.put("columnsOrdered", "true");
        paramMap.put("compressionType", "none");
        paramMap.put("delimiter", ",");
        paramMap.put("skip.header.line.count", "1");
        paramMap.put("typeOfData", "file");
        paramMap.put("serialization.format", ",");
        tableInput.setParameters(paramMap);
        createTableRequest.setTableInput(tableInput);
        CreateTableResult createTableResult = awsGlue.createTable(createTableRequest);
        log.debug(" glue  createTable Result :{}", createTableResult);
    }

    private void updateTable(RefTableVersionInfo refTableVersionInfo) {
        UpdateTableRequest updateTableRequest = new UpdateTableRequest();
        updateTableRequest.setDatabaseName(databaseName);
        StorageDescriptor storageDescriptor = new StorageDescriptor();
        storageDescriptor.setInputFormat("org.apache.hadoop.mapred.TextInputFormat");
        storageDescriptor.setOutputFormat("org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat");
        storageDescriptor.setCompressed(false);
        storageDescriptor.setNumberOfBuckets(-1);
        SerDeInfo serDeInfo = new SerDeInfo().withSerializationLibrary("org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe");
        serDeInfo.setParameters(
                ImmutableMap.of(
                        "field.delim", ",",
                        "serialization.format", ","));
        storageDescriptor.setSerdeInfo(serDeInfo);
        storageDescriptor.setLocation(refTableVersionInfo.getDataPath());
        LinkedHashSet<RefColumn> refTableColumns = refTableVersionInfo.getRefTableColumns();
        List<Column> columnList = refTableColumns.stream().map(refColumn -> new Column().withName(refColumn.getName()).withType(refColumn.getColumnType())).collect(Collectors.toList());
        storageDescriptor.setColumns(columnList);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("EXTERNAL", "TRUE");
        paramMap.put("classification", "csv");
        paramMap.put("columnsOrdered", "true");
        paramMap.put("compressionType", "none");
        paramMap.put("delimiter", ",");
        paramMap.put("skip.header.line.count", "1");
        paramMap.put("typeOfData", "file");
        paramMap.put("serialization.format", ",");

        TableInput tableInput = new TableInput();
        tableInput.setParameters(paramMap);
        tableInput.setName(refTableVersionInfo.getTableName());
        tableInput.setStorageDescriptor(storageDescriptor);
        tableInput.setTableType("EXTERNAL_TABLE");
        updateTableRequest.setTableInput(tableInput);
        updateTableRequest.setSkipArchive(true);
        UpdateTableResult updateTableResult = awsGlue.updateTable(updateTableRequest);
        log.debug(" glue updateTable Result :{}", updateTableResult);
    }
}
