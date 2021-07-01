package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.constant.StatisticsMode;
import com.miotech.kun.metadata.databuilder.extract.statistics.DatasetStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.statistics.DatasetStatisticsExtractorFactory;
import com.miotech.kun.metadata.databuilder.extract.tool.DataSourceBuilder;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class MSEBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MSEBuilder.class);

    private final MetadataDatasetDao datasetDao;
    private final DataSourceBuilder dataSourceBuilder;
    private final Loader loader;

    @Inject
    public MSEBuilder(MetadataDatasetDao datasetDao, DataSourceBuilder dataSourceBuilder, Loader loader) {
        this.datasetDao = datasetDao;
        this.dataSourceBuilder = dataSourceBuilder;
        this.loader = loader;
    }

    public void extractStatistics(Long gid, Long snapshotId, StatisticsMode statisticsMode)  {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin to extractStat, gid: {}", gid);
        }

        Optional<Dataset> dataset = datasetDao.fetchDatasetByGid(gid);
        if (!dataset.isPresent()) {
            logger.warn("Dataset not found, gid: {}", gid);
            return;
        }

        if (dataset.get().isDeleted()) {
            logger.warn("Dataset: {} has been marked as `deleted`", gid);
            return;
        }

        DataSource dataSource = dataSourceBuilder.fetchByGid(gid);
        DatasetStatisticsExtractor extractor = DatasetStatisticsExtractorFactory.createExtractor(dataSource.getType());
        boolean existed = extractor.judgeExistence(dataset.get(), dataSource, DatasetExistenceJudgeMode.DATASET);
        if (!existed) {
            logger.warn("Dataset: {} no longer existed， need to be marked as `deleted`", gid);
            return;
        }

        Dataset datasetWithStat = extractor.extract(dataset.get(), dataSource, statisticsMode);
        loader.loadStatistics(snapshotId, datasetWithStat);
    }
}
