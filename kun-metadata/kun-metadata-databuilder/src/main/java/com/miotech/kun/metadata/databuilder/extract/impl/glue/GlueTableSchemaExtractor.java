package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.amazonaws.services.glue.model.Table;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class GlueTableSchemaExtractor extends SchemaExtractorTemplate {

    private static final Logger logger = LoggerFactory.getLogger(GlueTableSchemaExtractor.class);

    private final AWSDataSource awsDataSource;

    private final Table table;

    public GlueTableSchemaExtractor(AWSDataSource awsDataSource, Table table) {
        super(awsDataSource.getId());
        this.awsDataSource = awsDataSource;
        this.table = table;
    }

    @Override
    protected List<DatasetField> getSchema() {
        return table.getStorageDescriptor().getColumns().stream().map(column -> DatasetField.newBuilder()
                .withName(column.getName())
                .withComment(column.getComment())
                .withFieldType(new DatasetFieldType(convertRawType(column.getType()), column.getType()))
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    protected DataStore getDataStore() {
        return new HiveTableStore(awsDataSource.getAthenaUrl(), table.getDatabaseName(), table.getName());
    }

    @Override
    protected String getName() {
        return table.getName();
    }

    @Override
    protected void close() {
    }

    private DatasetFieldType.Type convertRawType(String rawType) {
        if ("string".equals(rawType) ||
                rawType.startsWith("varchar") ||
                rawType.startsWith("char")) {
            return DatasetFieldType.Type.CHARACTER;
        } else if ("timestamp".equals(rawType) ||
                "date".equals(rawType)) {
            return DatasetFieldType.Type.DATETIME;
        } else if (rawType.startsWith("array")) {
            return DatasetFieldType.Type.ARRAY;
        } else if (rawType.startsWith("decimal") ||
                "double".equals(rawType) ||
                "number".equals(rawType) ||
                "int".equals(rawType) ||
                "smallint".equals(rawType) ||
                "bigint".equals(rawType)) {
            return DatasetFieldType.Type.NUMBER;
        } else if (rawType.startsWith("struct")) {
            return DatasetFieldType.Type.STRUCT;
        } else if ("boolean".equals(rawType) || "BOOL".equals(rawType)) {
            return DatasetFieldType.Type.BOOLEAN;
        } else {
            logger.warn("unknown type: {}", rawType);
            return DatasetFieldType.Type.UNKNOW;
        }
    }
}