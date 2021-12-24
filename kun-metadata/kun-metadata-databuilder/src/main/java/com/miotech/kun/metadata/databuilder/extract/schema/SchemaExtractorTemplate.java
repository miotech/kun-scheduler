package com.miotech.kun.metadata.databuilder.extract.schema;

import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class SchemaExtractorTemplate {

    private static final Logger logger = LoggerFactory.getLogger(SchemaExtractorTemplate.class);

    private final long datasourceId;

    public SchemaExtractorTemplate(long datasourceId) {
        this.datasourceId = datasourceId;
    }

    protected abstract List<DatasetField> getSchema();

    protected abstract DataStore getDataStore();

    protected abstract String getName();

    protected abstract void close();

    public final Dataset extract() {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();

        try {
            List<DatasetField> fields = getSchema();

            DataStore dataStore = getDataStore();
            if (logger.isDebugEnabled()) {
                logger.debug("SchemaExtractorTemplate extract getDataStore: {}", DataStoreJsonUtil.toJson(dataStore));
            }

            datasetBuilder.withName(getName())
                    .withDatasourceId(datasourceId)
                    .withFields(fields)
                    .withDataStore(dataStore);
        } catch (Exception e) {
            logger.error("SchemaExtractorTemplate extract error dataStore: {}", getDataStore(), e);
        } finally {
            close();
        }
        return datasetBuilder.build();
    }
}