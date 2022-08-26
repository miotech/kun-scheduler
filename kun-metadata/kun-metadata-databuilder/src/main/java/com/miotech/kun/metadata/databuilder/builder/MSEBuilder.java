package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.cataloger.Cataloger;
import com.miotech.kun.metadata.common.cataloger.CatalogerFactory;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.common.service.FilterRuleService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.constant.StatisticsMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
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

    private final MetadataDatasetService metadataDatasetService;
    private final DataSourceService dataSourceService;
    private final Loader loader;
    private final CatalogerFactory catalogerFactory;
    private final FilterRuleService filterRuleService;

    @Inject
    public MSEBuilder(MetadataDatasetService metadataDatasetService, DataSourceService dataSourceService, Loader loader,
                      CatalogerFactory catalogerFactory, FilterRuleService filterRuleService) {
        this.metadataDatasetService = metadataDatasetService;
        this.dataSourceService = dataSourceService;
        this.loader = loader;
        this.catalogerFactory = catalogerFactory;
        this.filterRuleService = filterRuleService;
    }

    public void extractStatistics(Long gid, Long snapshotId, StatisticsMode statisticsMode) {
        Optional<Dataset> dataset = metadataDatasetService.fetchDatasetByGid(gid);
        if (!dataset.isPresent()) {
            logger.warn("Dataset not found, gid: {}", gid);
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Begin to extractStat, gid: {}", gid);
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
        Cataloger cataloger = catalogerFactory.generateCataloger(dataSource);
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
