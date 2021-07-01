package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.google.common.annotations.VisibleForTesting;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.client.MetaStoreClient;
import com.miotech.kun.metadata.databuilder.context.ApplicationContext;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import com.miotech.kun.metadata.databuilder.service.fieldmapping.FieldMappingService;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;

import java.util.List;
import java.util.stream.Collectors;

public class HiveTableSchemaExtractor extends SchemaExtractorTemplate {

    private final String dbName;
    private final String tableName;
    private final HiveDataSource hiveDataSource;
    private final FieldMappingService fieldMappingService;
    private Table table;

    public HiveTableSchemaExtractor(HiveDataSource hiveDataSource, String dbName, String tableName) {
        super(hiveDataSource.getId());
        this.dbName = dbName;
        this.tableName = tableName;
        this.hiveDataSource = hiveDataSource;
        this.fieldMappingService = ApplicationContext.getContext().getInjector().getInstance(FieldMappingService.class);
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        HiveMetaStoreClient client = null;

        try {
            client = MetaStoreClient.getClient(hiveDataSource.getMetaStoreUris());
            table = client.getTable(dbName, tableName);

            return table.getSd().getCols().stream()
                    .map(schema -> new DatasetField(schema.getName(),
                            new DatasetFieldType(fieldMappingService.parse(hiveDataSource.getType().name(), schema.getType()), schema.getType()),
                            schema.getComment()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            MetaStoreClient.close(client);
        }

    }

    @Override
    protected DataStore getDataStore() {
        return new HiveTableStore(table.getSd().getLocation(), dbName, tableName);
    }

    @Override
    protected String getName() {
        return tableName;
    }

    @Override
    protected void close() {

    }

}