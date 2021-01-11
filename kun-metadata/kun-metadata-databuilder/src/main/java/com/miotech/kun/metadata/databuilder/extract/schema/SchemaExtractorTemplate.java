package com.miotech.kun.metadata.databuilder.extract.schema;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public abstract class SchemaExtractorTemplate implements Extractor {

    private static final Logger logger = LoggerFactory.getLogger(SchemaExtractorTemplate.class);

    private final long datasourceId;

    public SchemaExtractorTemplate(long datasourceId) {
        this.datasourceId = datasourceId;
    }

    protected abstract List<DatasetField> getSchema();

    protected abstract DataStore getDataStore();

    protected abstract String getName();

    protected abstract void close();

    @Override
    public Iterator<Dataset> extract() {
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
        return Iterators.forArray(datasetBuilder.build());
    }
}