package com.miotech.kun.metadata.databuilder.extract.template;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.core.model.*;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public abstract class ExtractorTemplate implements Extractor {
    private static final Logger logger = LoggerFactory.getLogger(ExtractorTemplate.class);

    private final long datasourceId;

    public ExtractorTemplate(long datasourceId) {
        this.datasourceId = datasourceId;
    }

    protected abstract List<DatasetField> getSchema();

    protected abstract DatasetFieldStat getFieldStats(DatasetField datasetField);

    protected abstract DatasetStat getTableStats();

    protected abstract DataStore getDataStore();

    protected abstract String getName();

    protected abstract void close();

    @Override
    public Iterator<Dataset> extract() {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();

        try {
            List<DatasetField> fields = getSchema();
            if (logger.isDebugEnabled()) {
                logger.debug("ExtractorTemplate extract getSchema: {}", JSONUtils.toJsonString(fields));
            }

            List<DatasetFieldStat> fieldStats = Lists.newArrayList();

            if (fields != null) {
                for (DatasetField datasetField : fields) {
                    DatasetFieldStat fieldStat = getFieldStats(datasetField);
                    if (logger.isDebugEnabled()) {
                        logger.debug("ExtractorTemplate extract getFieldStats: {}", JSONUtils.toJsonString(fieldStat));
                    }

                    if (fieldStat != null) {
                        fieldStats.add(fieldStat);
                    }
                }
            }

            DatasetStat tableStat = getTableStats();
            if (logger.isDebugEnabled()) {
                logger.debug("ExtractorTemplate extract getTableStats: {}", JSONUtils.toJsonString(tableStat));
            }

            DataStore dataStore = getDataStore();
            if (logger.isDebugEnabled()) {
                logger.debug("ExtractorTemplate extract getDataStore: {}", DataStoreJsonUtil.toJson(dataStore));
            }

            datasetBuilder.withName(getName())
                    .withDatasourceId(datasourceId)
                    .withFields(fields)
                    .withFieldStats(fieldStats)
                    .withDatasetStat(tableStat)
                    .withDataStore(dataStore);
        } catch (Exception e) {
            logger.error("ExtractorTemplate extract error dataStore: {}", getDataStore(), e);
            // TODO add retry
        } finally {
            close();
        }
        return Iterators.forArray(datasetBuilder.build());
    }
}
