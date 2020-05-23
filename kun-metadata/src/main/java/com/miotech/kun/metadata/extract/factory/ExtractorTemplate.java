package com.miotech.kun.metadata.extract.factory;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.workflow.core.model.entity.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public abstract class ExtractorTemplate implements Extractor {
    private static final Logger logger = LoggerFactory.getLogger(ExtractorTemplate.class);

    protected abstract List<DatasetField> getSchema();
    protected abstract DatasetFieldStat getFieldStats(DatasetField datasetField);
    protected abstract DatasetStat getTableStats();
    protected abstract DataStore getDataStore();

    @Override
    public Iterator<Dataset> extract() {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();
        datasetBuilder.withFields(getSchema())
                .withDatasetStat(getTableStats())
                .withDataStore(getDataStore());
        return Iterators.forArray(datasetBuilder.build());
    }
}
