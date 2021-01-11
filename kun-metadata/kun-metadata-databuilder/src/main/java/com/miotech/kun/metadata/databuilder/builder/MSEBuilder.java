package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.stat.DatasetStatExtractor;
import com.miotech.kun.metadata.databuilder.extract.stat.DatasetStatExtractorFactory;
import com.miotech.kun.metadata.databuilder.extract.tool.DataSourceBuilder;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.load.impl.PostgresLoader;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class MSEBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MSEBuilder.class);

    private final DatabaseOperator operator;
    private final MetadataDatasetDao datasetDao;
    private final DataSourceBuilder dataSourceBuilder;

    @Inject
    public MSEBuilder(DatabaseOperator operator, MetadataDatasetDao datasetDao, DataSourceBuilder dataSourceBuilder) {
        this.operator = operator;
        this.datasetDao = datasetDao;
        this.dataSourceBuilder = dataSourceBuilder;
    }

    public void extractStat(Long gid) throws Exception {
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
        DatasetStatExtractor extractor = DatasetStatExtractorFactory.createExtractor(dataSource.getType());
        boolean existed = extractor.judgeExistence(dataset.get(), dataSource, DatasetExistenceJudgeMode.DATASET);
        if (!existed) {
            logger.warn("Dataset: {} no longer existed， need to be marked as `deleted`", gid);
            return;
        }

        Dataset datasetWithStat = extractor.extract(dataset.get(), dataSource);
        Loader loader = new PostgresLoader(operator);
        loader.loadStat(datasetWithStat);

    }
}
