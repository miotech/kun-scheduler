package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.cataloger.Cataloger;
import com.miotech.kun.metadata.common.cataloger.CatalogerConfig;
import com.miotech.kun.metadata.common.cataloger.CatalogerFactory;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.constant.StatisticsMode;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.databuilder.extract.statistics.DatasetStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.statistics.DatasetStatisticsExtractorFactory;
import com.miotech.kun.metadata.databuilder.load.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class MSEBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MSEBuilder.class);

    private final MetadataDatasetDao datasetDao;
    private final DataSourceService dataSourceService;
    private final Loader loader;
    private final CatalogerFactory catalogerFactory;

    @Inject
    public MSEBuilder(MetadataDatasetDao datasetDao, DataSourceService dataSourceService, Loader loader,
                      CatalogerFactory catalogerFactory) {
        this.datasetDao = datasetDao;
        this.dataSourceService = dataSourceService;
        this.loader = loader;
        this.catalogerFactory = catalogerFactory;
    }

    public void extractStatistics(Long gid, Long snapshotId, StatisticsMode statisticsMode) {
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

        Optional<DataSource> dataSourceOptional = dataSourceService.getDatasourceById(dataset.get().getDatasourceId());
        if (!dataSourceOptional.isPresent()) {
            logger.warn("Datasource not found, datasourceId: {}", dataset.get().getDatasourceId());
            return;
        }
        DataSource dataSource = dataSourceOptional.get();
        CatalogerConfig config = CatalogerConfig.newBuilder().build();
        Cataloger cataloger = catalogerFactory.generateCataloger(dataSource, config);
        DatasetStatisticsExtractor extractor = DatasetStatisticsExtractorFactory.createExtractor(dataSource, dataset.get(), cataloger);
        boolean existed = extractor.judgeExistence(dataset.get(), DatasetExistenceJudgeMode.DATASET);
        if (!existed) {
            logger.warn("Dataset: {} no longer existed， need to be marked as `deleted`", gid);
            return;
        }

        Dataset datasetWithStat = extractor.extract(statisticsMode);
        loader.loadStatistics(snapshotId, datasetWithStat);
    }
}
