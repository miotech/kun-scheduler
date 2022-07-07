package com.miotech.kun.datadiscovery.service.rdm.data;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.*;
import com.google.common.collect.ImmutableMap;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.service.rdm.RefDataOperator;
import com.miotech.kun.datadiscovery.service.rdm.file.S3StorageFileManger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    @Override
    public AmazonWebServiceResult overwrite(RefTableVersionInfo refTableVersionInfo) {
        String databaseName = refTableVersionInfo.getDatabaseName();
        String tableName = refTableVersionInfo.getTableName();
        if (exists(databaseName, tableName)) {
            return updateTable(refTableVersionInfo);
        }
        return createTable(refTableVersionInfo);
    }

    @Override
    public DeleteTableResult remove(String databaseName, String tableName) {
        DeleteTableRequest deleteTableRequest = new DeleteTableRequest().withDatabaseName(databaseName).withName(tableName);
        DeleteTableResult deleteTableResult = awsGlue.deleteTable(deleteTableRequest);
        log.debug("glue deleteTable Result:{}", deleteTableResult);
        return deleteTableResult;
    }

    private boolean exists(String databaseName, String tableName) {
        GetTableRequest tablesRequest = new GetTableRequest().withDatabaseName(databaseName).withName(tableName);
        try {
            awsGlue.getTable(tablesRequest);
            return true;
        } catch (EntityNotFoundException e) {
            log.debug("table is not found:{}", tablesRequest);
            return false;
        }

    }

    private CreateTableResult createTable(RefTableVersionInfo refTableVersionInfo) {
        CreateTableRequest createTableRequest = new CreateTableRequest();
        createTableRequest.setDatabaseName(refTableVersionInfo.getDatabaseName());
        TableInput tableInput = new TableInput();
        tableInput.setName(refTableVersionInfo.getTableName());
        StorageDescriptor storageDescriptor = new StorageDescriptor();
        storageDescriptor.setInputFormat("org.apache.hadoop.mapred.TextInputFormat");
        storageDescriptor.setOutputFormat("org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat");
        storageDescriptor.setCompressed(false);
        storageDescriptor.setNumberOfBuckets(-1);
        SerDeInfo serDeInfo = new SerDeInfo().withSerializationLibrary("org.apache.hadoop.hive.serde2.OpenCSVSerde");
        serDeInfo.setParameters(
                ImmutableMap.of(
                        "field.delim", ",",
                        "serialization.format", ","));
        storageDescriptor.setSerdeInfo(serDeInfo);
        storageDescriptor.setLocation(S3StorageFileManger.getTableVersionPath(refTableVersionInfo));
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
        paramMap.put("typeOfData", "file");
        paramMap.put("serialization.format", ",");
        tableInput.setParameters(paramMap);
        createTableRequest.setTableInput(tableInput);
        CreateTableResult createTableResult = awsGlue.createTable(createTableRequest);
        log.debug(" glue  createTable Result :{}", createTableResult);
        return createTableResult;
    }

    private UpdateTableResult updateTable(RefTableVersionInfo refTableVersionInfo) {
        UpdateTableRequest updateTableRequest = new UpdateTableRequest();
        updateTableRequest.setDatabaseName(refTableVersionInfo.getDatabaseName());
        StorageDescriptor storageDescriptor = new StorageDescriptor();
        storageDescriptor.setInputFormat("org.apache.hadoop.mapred.TextInputFormat");
        storageDescriptor.setOutputFormat("org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat");
        storageDescriptor.setCompressed(false);
        storageDescriptor.setNumberOfBuckets(-1);
        SerDeInfo serDeInfo = new SerDeInfo().withSerializationLibrary("org.apache.hadoop.hive.serde2.OpenCSVSerde");
        serDeInfo.setParameters(
                ImmutableMap.of(
                        "field.delim", ",",
                        "serialization.format", ","));
        storageDescriptor.setSerdeInfo(serDeInfo);
        storageDescriptor.setLocation(S3StorageFileManger.getTableVersionPath(refTableVersionInfo));
        LinkedHashSet<RefColumn> refTableColumns = refTableVersionInfo.getRefTableColumns();
        List<Column> columnList = refTableColumns.stream().map(refColumn -> new Column().withName(refColumn.getName()).withType(refColumn.getColumnType())).collect(Collectors.toList());
        storageDescriptor.setColumns(columnList);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("EXTERNAL", "TRUE");
        paramMap.put("classification", "csv");
        paramMap.put("columnsOrdered", "true");
        paramMap.put("compressionType", "none");
        paramMap.put("delimiter", ",");
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
        return updateTableResult;
    }
}
