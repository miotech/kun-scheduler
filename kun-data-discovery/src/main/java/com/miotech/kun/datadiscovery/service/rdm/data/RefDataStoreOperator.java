package com.miotech.kun.datadiscovery.service.rdm.data;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.*;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import com.miotech.kun.datadiscovery.service.rdm.RefDataOperator;
import com.miotech.kun.datadiscovery.service.rdm.file.ParquetUtils;
import com.miotech.kun.datadiscovery.service.rdm.file.S3StoragePathGenerator;
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
        LinkedHashSet<RefColumn> refTableColumns = refTableVersionInfo.getRefTableColumns();
        checkColumns(refTableColumns, refTableVersionInfo);
        String location = S3StoragePathGenerator.getTableVersionPath(refTableVersionInfo);
        CreateTableRequest createTableRequest = new CreateTableRequest();
        createTableRequest.setDatabaseName(refTableVersionInfo.getDatabaseName());
        TableInput tableInput = getTableInput(refTableVersionInfo, location);
        createTableRequest.setTableInput(tableInput);
        CreateTableResult createTableResult = awsGlue.createTable(createTableRequest);
        log.debug(" glue  createTable Result :{}", createTableResult);
        return createTableResult;
    }

    private UpdateTableResult updateTable(RefTableVersionInfo refTableVersionInfo) {
        LinkedHashSet<RefColumn> refTableColumns = refTableVersionInfo.getRefTableColumns();
        checkColumns(refTableColumns, refTableVersionInfo);
        String location = S3StoragePathGenerator.getTableVersionPath(refTableVersionInfo);
        UpdateTableRequest updateTableRequest = new UpdateTableRequest();
        updateTableRequest.setDatabaseName(refTableVersionInfo.getDatabaseName());
        TableInput tableInput = getTableInput(refTableVersionInfo, location);
        updateTableRequest.setTableInput(tableInput);
        updateTableRequest.setSkipArchive(true);
        UpdateTableResult updateTableResult = awsGlue.updateTable(updateTableRequest);
        log.debug(" glue updateTable Result :{}", updateTableResult);
        return updateTableResult;
    }


    private TableInput getTableInput(RefTableVersionInfo refTableVersionInfo, String location) {
        TableInput tableInput = new TableInput();
        tableInput.setName(refTableVersionInfo.getTableName());
        tableInput.setTableType("EXTERNAL_TABLE");
        tableInput.setStorageDescriptor(getStorageDescriptor(refTableVersionInfo.getRefTableColumns(), location));
        tableInput.setParameters(tableParamMap(refTableVersionInfo));
        return tableInput;
    }

    private StorageDescriptor getStorageDescriptor(LinkedHashSet<RefColumn> refTableColumns, String location) {
        StorageDescriptor storageDescriptor = new StorageDescriptor();
        storageDescriptor.setInputFormat("org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat");
        storageDescriptor.setOutputFormat("org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat");
        storageDescriptor.setCompressed(false);
        storageDescriptor.setNumberOfBuckets(-1);
        storageDescriptor.setSerdeInfo(getSerDeInfo(location));
        storageDescriptor.setLocation(location);
        List<Column> columnList = refTableColumns.stream()
                .map(refColumn -> new Column()
                        .withName(refColumn.getName())
                        .withType(ColumnType.columnType(refColumn.getColumnType()).getSparkDataType().typeName()))
                .collect(Collectors.toList());
        storageDescriptor.setColumns(columnList);
        return storageDescriptor;
    }

    private SerDeInfo getSerDeInfo(String location) {
        SerDeInfo serDeInfo = new SerDeInfo().withSerializationLibrary("org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe");
        Map<String, String> paramMap_ser = new HashMap<>();
        paramMap_ser.put("path", location);
        paramMap_ser.put("serialization.format", "1");
        serDeInfo.setParameters(paramMap_ser);
        return serDeInfo;
    }

    private Map<String, String> tableParamMap(RefTableVersionInfo refTableVersionInfo) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("EXTERNAL", "TRUE");
        paramMap.put("spark.sql.sources.schema.part.0", ParquetUtils.sparkSchemaJson(ParquetUtils.createMessageType(refTableVersionInfo.getSchemaName(), refTableVersionInfo.getRefTableColumns())));
        paramMap.put("spark.sql.sources.schema.numParts", "1");
        paramMap.put("spark.sql.sources.provider", "parquet");
        paramMap.put("spark.sql.create.version", "2.4.4");
        return paramMap;
    }


    private void checkColumns(LinkedHashSet<RefColumn> refTableColumns, RefTableVersionInfo refTableVersionInfo) {
        if (CollectionUtils.isEmpty(refTableColumns)) {
            log.error("columns is not empty:version id :{}", refTableVersionInfo.getVersionId());
            throw new IllegalStateException(String.format("columns is not empty:version id :%s", refTableVersionInfo.getVersionId()));
        }
    }
}
