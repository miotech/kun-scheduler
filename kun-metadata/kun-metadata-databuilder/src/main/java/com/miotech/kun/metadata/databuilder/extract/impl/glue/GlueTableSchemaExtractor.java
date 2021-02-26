package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.amazonaws.services.glue.model.Table;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.context.ApplicationContext;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.service.fieldmapping.FieldMappingService;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;

import java.util.List;
import java.util.stream.Collectors;

public class GlueTableSchemaExtractor extends SchemaExtractorTemplate {

    private final AWSDataSource awsDataSource;
    private final Table table;
    private final FieldMappingService fieldMappingService;

    public GlueTableSchemaExtractor(AWSDataSource awsDataSource, Table table) {
        super(awsDataSource.getId());
        this.awsDataSource = awsDataSource;
        this.table = table;
        this.fieldMappingService = ApplicationContext.getContext().getInjector().getInstance(FieldMappingService.class);
    }

    @Override
    protected List<DatasetField> getSchema() {
        return table.getStorageDescriptor().getColumns().stream().map(column -> DatasetField.newBuilder()
                .withName(column.getName())
                .withComment(column.getComment())
                .withFieldType(new DatasetFieldType(fieldMappingService.parse(awsDataSource.getType().name(), column.getType()), column.getType()))
                .withIsPrimaryKey(false)
                .withIsNullable(true)
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    protected DataStore getDataStore() {
        return new HiveTableStore(table.getStorageDescriptor().getLocation(), table.getDatabaseName(), table.getName());
    }

    @Override
    protected String getName() {
        return table.getName();
    }

    @Override
    protected void close() {
        // do nothing
    }

}