package com.miotech.kun.metadata.extract.factory;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;

import java.util.Iterator;
import java.util.List;

public abstract class ExtractorTemplate implements Extractor {

    public abstract List<DatasetField> getSchema();
    public abstract DatasetFieldStat getFieldStats(DatasetField datasetField);
    public abstract DatasetStat getTableStats();

    @Override
    public Iterator<Dataset> extract() {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();
        datasetBuilder.withFields(getSchema()).withDatasetStat(null).withDataStore(null).build();
        return Iterators.forArray(datasetBuilder.build());
    }
}
