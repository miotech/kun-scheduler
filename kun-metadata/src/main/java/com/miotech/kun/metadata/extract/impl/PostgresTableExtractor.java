package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.extract.factory.ExtractorTemplate;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.metadata.model.PostgresCluster;
import com.miotech.kun.workflow.core.model.lineage.DataStore;

import java.util.List;

public class PostgresTableExtractor extends ExtractorTemplate {

    private final PostgresCluster cluster;
    private final String table;

    public PostgresTableExtractor(PostgresCluster cluster, String table) {
        this.cluster = cluster;
        this.table = table;
    }

    @Override
    public List<DatasetField> getSchema() {
        return null;
    }

    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        return null;
    }

    @Override
    public DatasetStat getTableStats() {
        return null;
    }

    @Override
    protected DataStore getDataStore() {
        return null;
    }
}
