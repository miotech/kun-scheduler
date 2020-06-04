package com.miotech.kun.metadata.extract.template;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public abstract class ExtractorTemplate implements Extractor {
    private static final Logger logger = LoggerFactory.getLogger(ExtractorTemplate.class);

    protected final Cluster cluster;

    public ExtractorTemplate(Cluster cluster) {
        this.cluster = cluster;
    }

    protected abstract List<DatasetField> getSchema();

    protected abstract DatasetFieldStat getFieldStats(DatasetField datasetField);

    protected abstract DatasetStat getTableStats();

    protected abstract DataStore getDataStore();

    protected abstract String getName();

    protected long getClusterId() {
        return cluster.getClusterId();
    }

    @Override
    public Iterator<Dataset> extract() {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();

        try {
            List<DatasetField> fields = getSchema();
            logger.debug("ExtractorTemplate extract getSchema: {}", JSONUtils.toJsonString(fields));
            List<DatasetFieldStat> fieldStats = Lists.newArrayList();

            if (fields != null) {
                for (DatasetField datasetField : fields) {
                    DatasetFieldStat fieldStat = getFieldStats(datasetField);
                    logger.debug("ExtractorTemplate extract getFieldStats: {}", JSONUtils.toJsonString(fieldStat));
                    if (fieldStat != null) {
                        fieldStats.add(fieldStat);
                    }
                }
            }

            DatasetStat tableStat = getTableStats();
            logger.debug("ExtractorTemplate extract getTableStats: {}", JSONUtils.toJsonString(tableStat));

            DataStore dataStore = getDataStore();
            logger.debug("ExtractorTemplate extract getDataStore: {}", DataStoreJsonUtil.toJson(dataStore));
            datasetBuilder.withName(getName())
                    .withClusterId(getClusterId())
                    .withFields(fields)
                    .withFieldStats(fieldStats)
                    .withDatasetStat(tableStat)
                    .withDataStore(dataStore);
        } catch (Exception e) {
            logger.error("ExtractorTemplate extract error dataStore: {}", getDataStore(), e);
            // TODO add retry
        }
        return Iterators.forArray(datasetBuilder.build());
    }
}
