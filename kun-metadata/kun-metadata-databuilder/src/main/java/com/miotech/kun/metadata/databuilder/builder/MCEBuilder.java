package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.utils.HttpClientUtil;
import com.miotech.kun.metadata.common.cataloger.Cataloger;
import com.miotech.kun.metadata.common.cataloger.CatalogerFactory;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.common.service.FilterRuleService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.event.DatasetCreatedEvent;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;
import com.miotech.kun.metadata.core.model.event.MetadataStatisticsEvent;
import com.miotech.kun.metadata.core.model.filter.FilterRuleType;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.LoadSchemaResult;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.metadata.core.model.constant.OperatorKey.MSE_URL;

@Singleton
public class MCEBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MCEBuilder.class);

    private final Props props;
    private final MetadataDatasetService metadataDatasetService;
    private final DataSourceService dataSourceService;
    private final FilterRuleService filterRuleService;
    private final Loader loader;
    private final HttpClientUtil httpClientUtil;
    private final CatalogerFactory catalogerFactory;
    private final EventPublisher publisher;

    @Inject
    public MCEBuilder(Props props, MetadataDatasetService metadataDatasetService,
                      DataSourceService dataSourceService, Loader loader, HttpClientUtil httpClientUtil,
                      CatalogerFactory catalogerFactory, EventPublisher publisher, FilterRuleService filterRuleService) {
        this.props = props;
        this.metadataDatasetService = metadataDatasetService;
        this.dataSourceService = dataSourceService;
        this.loader = loader;
        this.httpClientUtil = httpClientUtil;
        this.catalogerFactory = catalogerFactory;
        this.publisher = publisher;
        this.filterRuleService = filterRuleService;
    }

    public void extractSchemaOfDataSource(Long datasourceId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin to extractSchemaOfDataSource, datasourceId: {}", datasourceId);
        }
        Optional<DataSource> dataSourceOptional = dataSourceService.getDatasourceById(datasourceId);
        if (!dataSourceOptional.isPresent()) {
            logger.warn("datasource not exists,id = {}", datasourceId);
            return;
        }
        DataSource dataSource = dataSourceOptional.get();
        Cataloger cataloger = catalogerFactory.generateCataloger(dataSource);
        List<Dataset> datasets = metadataDatasetService.getListByDatasource(datasourceId);
        for (Dataset dataset : datasets) {
            try {
                String mceRule = getMceRule(dataSource, dataset);
                if (filterRuleService.judge(FilterRuleType.MCE, mceRule)) {
                    boolean accept = judgeDatasetExistence(Optional.of(dataset), cataloger, DatasetExistenceJudgeMode.SNAPSHOT);
                    metadataDatasetService.updateDatasetStatus(accept, dataset.getGid());
                } else {
                    logger.debug("filter judge false,{}", mceRule);
                }

            } catch (Exception e) {
                logger.error("Extract schema error: ", e);
                logger.error("gid: {}", dataset.getGid());
            }
        }
        Set<String> datasetDsiList = datasets.stream().map(Dataset::getDSI).collect(Collectors.toSet());
        Iterator<Dataset> datasetIterator = cataloger.extract(dataSource);
        while (datasetIterator.hasNext()) {
            try {
                Dataset dataset = datasetIterator.next();
                Boolean accept = filterRuleService.judge(FilterRuleType.MCE, getMceRule(dataSource, dataset));
                if (Boolean.FALSE.equals(accept)) {
                    continue;
                }
                LoadSchemaResult loadSchemaResult = loader.loadSchema(dataset);
                if (datasetDsiList.contains(dataset.getDSI()) || loadSchemaResult.getGid() < 0) {
                    continue;
                }
                //send dataset created event
                DatasetCreatedEvent datasetCreatedEvent = new DatasetCreatedEvent(loadSchemaResult.getGid(), dataset.getDatasourceId(),
                        dataset.getDatabaseName(), dataset.getName());
                logger.info("going to push dataset created event: {}", datasetCreatedEvent);
                publisher.publish(datasetCreatedEvent);
            } catch (Exception e) {
                logger.error("Load schema error: ", e);
            }
        }
    }


    public void extractSchemaOfDataset(Long gid) {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin to extractSchemaOfDataset, gid: {}", gid);
        }

        Optional<Dataset> dataset = metadataDatasetService.fetchDatasetByGid(gid);
        if (Boolean.FALSE.equals(dataset.isPresent())) {
            logger.warn("Dataset not found, gid: {}", gid);
            return;
        }

        Optional<DataSource> dataSourceOptional = dataSourceService.getDatasourceById(dataset.get().getDatasourceId());
        if (Boolean.FALSE.equals(dataSourceOptional.isPresent())) {
            logger.warn("datasource not exists,id = {}", dataset.get().getDatasourceId());
            return;
        }
        DataSource dataSource = dataSourceOptional.get();
        Cataloger cataloger = catalogerFactory.generateCataloger(dataSource);
        String mceRule = getMceRule(dataSource, dataset.get());
        logger.info("filter judge :{}", mceRule);
        if (Boolean.FALSE.equals(filterRuleService.judge(FilterRuleType.MCE, mceRule))) {
            logger.debug("filter judge false,{}", mceRule);
            return;
        }
        boolean accept = judgeDatasetExistence(dataset, cataloger, DatasetExistenceJudgeMode.DATASET);
        metadataDatasetService.updateDatasetStatus(accept, gid);
        if (Boolean.FALSE.equals(accept)) {
            return;
        }

        List<DatasetField> fields = cataloger.extract(dataset.get());
        LoadSchemaResult loadSchemaResult = loader.loadSchema(gid, fields);
        // 发送消息
        sendMseEvent(loadSchemaResult, dataset.get());

    }

    public void handlePushEvent(String message) {
        MetadataChangeEvent mce = JSONUtils.jsonToObject(message, MetadataChangeEvent.class);
        Optional<DataSource> fetchedDataSource = dataSourceService.getDatasourceById(mce.getDataSourceId());
        if (Boolean.FALSE.equals(fetchedDataSource.isPresent())) {
            logger.warn("datasourceId = {} not exist", mce.getDataSourceId());
            return;
        }
        Optional<Dataset> dataset = metadataDatasetService.fetchDataSet(mce.getDataSourceId(), mce.getDatabaseName(), mce.getTableName());
        if (dataset.isPresent()) {
            extractSchemaOfDataset(dataset.get().getGid());
            return;
        }
        Cataloger cataloger = catalogerFactory.generateCataloger(fetchedDataSource.get());
        Dataset newDataset = null;
        try {
            newDataset = cataloger.extract(mce);
        } catch (Exception e) {
            logger.error("cataloger error:{}", mce, e);
            return;
        }
        if (Objects.isNull(newDataset)) {
            logger.debug("dataset not exist:{}", mce);
            return;
        }
        String mceRule = getMceRule(fetchedDataSource.get(), newDataset);
        logger.info("filter judge :{}", mceRule);
        if (Boolean.FALSE.equals(filterRuleService.judge(FilterRuleType.MCE, mceRule))) {
            logger.debug("filter judge false,{}", mceRule);
            return;
        }
        LoadSchemaResult loadSchemaResult = loader.loadSchema(newDataset);
        //send dataset created event
        DatasetCreatedEvent datasetCreatedEvent = new DatasetCreatedEvent(loadSchemaResult.getGid(), newDataset.getDatasourceId(),
                newDataset.getDatabaseName(), newDataset.getName());
        logger.info("going to push dataset created event: {}", datasetCreatedEvent);
        publisher.publish(datasetCreatedEvent);

        // 发送消息
        sendMseEvent(loadSchemaResult, newDataset);

    }

    private void sendMseEvent(LoadSchemaResult loadSchemaResult, Dataset dataset) {
        Optional<DataSource> fetchedDataSource = dataSourceService.getDatasourceById(dataset.getDatasourceId());
        if (Boolean.FALSE.equals(fetchedDataSource.isPresent())) {
            logger.warn("datasource not exists,id = {}", dataset.getDatasourceId());
            return;
        }
        String mseRule = getMseRule(fetchedDataSource.get(), dataset);
        // 发送消息
        logger.info("filter  mse judge :{}", mseRule);

        if (Boolean.FALSE.equals(filterRuleService.judge(FilterRuleType.MSE, mseRule))) {
            logger.debug("filter mse:{}", mseRule);
            return;
        }
        try {
            MetadataStatisticsEvent mse = new MetadataStatisticsEvent(MetadataStatisticsEvent.EventType.FIELD, loadSchemaResult.getGid(), loadSchemaResult.getSnapshotId());
            httpClientUtil.doPost(props.getString(MSE_URL), JSONUtils.toJsonString(mse));
        } catch (Exception e) {
            logger.warn("call mse execute api error: ", e);
        }
    }

    private boolean judgeDatasetExistence(Optional<Dataset> dataset, Cataloger cataloger, DatasetExistenceJudgeMode judgeMode) {
        return dataset.filter(value -> cataloger.judgeExistence(value, judgeMode)).isPresent();

    }

    private String getMceRule(DataSource dataSource, Dataset dataset) {
        return FilterRuleType.mceRule(dataSource.getDatasourceType().name(), dataSource.getName(), dataset.getDatabaseName(), dataset.getName());
    }

    private String getMseRule(DataSource dataSource, Dataset dataset) {
        return FilterRuleType.mseRule(dataSource.getDatasourceType().name(), dataSource.getName(), dataset.getDatabaseName(), dataset.getName());
    }


}
