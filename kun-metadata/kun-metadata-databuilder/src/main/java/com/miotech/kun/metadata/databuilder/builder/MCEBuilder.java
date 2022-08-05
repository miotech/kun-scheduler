package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.commons.web.utils.HttpClientUtil;
import com.miotech.kun.metadata.common.cataloger.*;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.constant.DatasetLifecycleStatus;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.event.DatasetCreatedEvent;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;
import com.miotech.kun.metadata.core.model.event.MetadataStatisticsEvent;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.LoadSchemaResult;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.metadata.core.model.constant.OperatorKey.*;

@Singleton
public class MCEBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MCEBuilder.class);

    private final Props props;
    private final DatabaseOperator operator;
    private final MetadataDatasetDao datasetDao;
    private final DataSourceService dataSourceService;
    private final Loader loader;
    private final HttpClientUtil httpClientUtil;
    private final CatalogerFactory catalogerFactory;
    private final EventPublisher publisher;

    @Inject
    public MCEBuilder(Props props, DatabaseOperator operator, MetadataDatasetDao datasetDao,
                      DataSourceService dataSourceService, Loader loader, HttpClientUtil httpClientUtil,
                      CatalogerFactory catalogerFactory, EventPublisher publisher) {
        this.props = props;
        this.operator = operator;
        this.datasetDao = datasetDao;
        this.dataSourceService = dataSourceService;
        this.loader = loader;
        this.httpClientUtil = httpClientUtil;
        this.catalogerFactory = catalogerFactory;
        this.publisher = publisher;
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
        CatalogerConfig config = generateCatalogerConfig(dataSource.getDatasourceType());
        Cataloger cataloger = catalogerFactory.generateCataloger(dataSource, config);
        List<Long> gids = operator.fetchAll("SELECT gid FROM kun_mt_dataset WHERE datasource_id = ?",
                rs -> rs.getLong("gid"), datasourceId);
        for (Long gid : gids) {
            try {
                boolean existed = judgeDatasetExistence(gid, cataloger, DatasetExistenceJudgeMode.SNAPSHOT);
                updateDatasetStatus(existed, gid);
            } catch (Exception e) {
                logger.error("Extract schema error: ", e);
                logger.error("gid: {}", gid);
            }
        }

        Iterator<Dataset> datasetIterator = cataloger.extract(dataSource);
        while (datasetIterator.hasNext()) {
            try {
                Dataset dataset = datasetIterator.next();
                Dataset savedDataset = datasetDao.fetchDataSet(dataset.getDatasourceId(), dataset.getDatabaseName(), dataset.getName());
                LoadSchemaResult loadSchemaResult = loader.loadSchema(dataset);
                if (savedDataset != null || loadSchemaResult.getGid() < 0) {
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

        Optional<Dataset> dataset = datasetDao.fetchDatasetByGid(gid);
        if (!dataset.isPresent()) {
            logger.warn("Dataset not found, gid: {}", gid);
            return;
        }

        Optional<DataSource> dataSourceOptional = dataSourceService.getDatasourceById(dataset.get().getDatasourceId());
        if (!dataSourceOptional.isPresent()) {
            logger.warn("datasource not exists,id = {}", dataset.get().getDatasourceId());
            return;
        }
        DataSource dataSource = dataSourceOptional.get();
        CatalogerConfig config = generateCatalogerConfig(dataSource.getDatasourceType());
        Cataloger cataloger = catalogerFactory.generateCataloger(dataSource, config);
        boolean existed = judgeDatasetExistence(dataset, cataloger, DatasetExistenceJudgeMode.DATASET);
        updateDatasetStatus(existed, gid);
        if (!existed) {
            return;
        }

        List<DatasetField> fields = cataloger.extract(dataset.get());
        LoadSchemaResult loadSchemaResult = loader.loadSchema(gid, fields);

        // 发送消息
        sendMseEvent(loadSchemaResult);

    }

    public void handlePushEvent(String message) {
        MetadataChangeEvent mce = JSONUtils.jsonToObject(message, MetadataChangeEvent.class);
        Optional<DataSource> fetchedDataSource = dataSourceService.getDatasourceById(mce.getDataSourceId());
        if (!fetchedDataSource.isPresent()) {
            logger.warn("datasourceId = {} not exist", mce.getDataSourceId());
            return;
        }
        Dataset dataset = datasetDao.fetchDataSet(mce.getDataSourceId(), mce.getDatabaseName(), mce.getTableName());
        DataSource dataSource = fetchedDataSource.get();
        CatalogerConfig config = generateCatalogerConfig(dataSource.getDatasourceType());
        Cataloger cataloger = catalogerFactory.generateCataloger(fetchedDataSource.get(), config);
        if (dataset != null) {
            extractSchemaOfDataset(dataset.getGid());
            return;
        }
        Dataset newDataset = cataloger.extract(mce);
        LoadSchemaResult loadSchemaResult = loader.loadSchema(newDataset);

        //send dataset created event
        if(newDataset != null){
            DatasetCreatedEvent datasetCreatedEvent = new DatasetCreatedEvent(loadSchemaResult.getGid(), newDataset.getDatasourceId(),
                    newDataset.getDatabaseName(), newDataset.getName());
            logger.info("going to push dataset created event: {}", datasetCreatedEvent);
            publisher.publish(datasetCreatedEvent);
        }

        // 发送消息
        sendMseEvent(loadSchemaResult);

    }

    private void sendMseEvent(LoadSchemaResult loadSchemaResult) {
        // 发送消息
        try {
            MetadataStatisticsEvent mse = new MetadataStatisticsEvent(MetadataStatisticsEvent.EventType.FIELD, loadSchemaResult.getGid(), loadSchemaResult.getSnapshotId());
            httpClientUtil.doPost(props.getString(MSE_URL), JSONUtils.toJsonString(mse));
        } catch (Exception e) {
            logger.warn("call mse execute api error: ", e);
        }
    }

    private boolean judgeDatasetExistence(long gid, Cataloger cataloger, DatasetExistenceJudgeMode judgeMode) {
        Optional<Dataset> dataset = datasetDao.fetchDatasetByGid(gid);
        if (!dataset.isPresent()) {
            logger.warn("Dataset not found, gid: {}", gid);
            return false;
        }

        return judgeDatasetExistence(dataset, cataloger, judgeMode);
    }

    private boolean judgeDatasetExistence(Optional<Dataset> dataset, Cataloger cataloger, DatasetExistenceJudgeMode judgeMode) {
        if (!dataset.isPresent()) {
            return false;
        }

        return cataloger.judgeExistence(dataset.get(), judgeMode);
    }

    private void updateDatasetStatus(boolean existed, long gid) {
        if (existed) {
            int updateRowCount = operator.update("UPDATE kun_mt_dataset SET deleted = false WHERE gid = ? AND deleted is true", gid);
            operator.update("UPDATE kun_mt_universal_search SET deleted = false WHERE gid = ? AND resource_type='DATASET' AND deleted is true", gid);
            if (updateRowCount == 1) {
                // 记录MANAGED事件
                operator.update("INSERT INTO kun_mt_dataset_lifecycle(dataset_gid, status, create_at) VALUES(?, ?, ?)", gid, DatasetLifecycleStatus.MANAGED.name(), DateTimeUtils.now());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Dataset: {} no longer existed, marked as `deleted`", gid);
            }

            int updateRowCount = operator.update("UPDATE kun_mt_dataset SET deleted = true WHERE gid = ? AND deleted is false", gid);
            operator.update("UPDATE kun_mt_universal_search SET deleted = true WHERE gid = ? AND resource_type='DATASET' AND deleted is false", gid);
            if (updateRowCount == 1) {
                // 记录DELETED事件
                operator.update("INSERT INTO kun_mt_dataset_lifecycle(dataset_gid, status, create_at) VALUES(?, ?, ?)", gid, DatasetLifecycleStatus.DELETED.name(), DateTimeUtils.now());
            }
        }
    }

    private CatalogerConfig generateCatalogerConfig(DatasourceType datasourceType) {
        CatalogerWhiteList defaultWhiteList = loadDefaultWhiteList(datasourceType);
        CatalogerBlackList defaultBlackList = loadDefaultBlackList(datasourceType);
        CatalogerWhiteList whiteList = defaultWhiteList.merge(getWhiteListFromProps());
        CatalogerBlackList blackList = defaultBlackList.merge(getBlackListFromProps());
        return new CatalogerConfig(whiteList, blackList);
    }

    private CatalogerWhiteList loadDefaultWhiteList(DatasourceType datasourceType) {
        logger.debug("load default whitelist...");
        List<String> whiteList = new ArrayList<>();
        try {
            Props whiteListProps = PropsUtils.loadPropsFromResource("catalogerWhiteList.yaml");
            whiteList = whiteListProps.getStringList(datasourceType.name());
            logger.debug("default blacklist is : " + whiteList.stream().collect(Collectors.joining(",")));
        } catch (Exception e) {
            logger.debug("whitelist for {} not exits", datasourceType.name());
        }
        return new CatalogerWhiteList(new HashSet<>(whiteList));
    }

    private CatalogerBlackList loadDefaultBlackList(DatasourceType datasourceType) {
        logger.debug("load default blacklist...");
        List<String> blackList = new ArrayList<>();
        try {
            Props blackListProps = PropsUtils.loadPropsFromResource("catalogerBlackList.yaml");
            blackList = blackListProps.getStringList(datasourceType.name());
            logger.debug("default blacklist is : " + blackList.stream().collect(Collectors.joining(",")));
        } catch (Exception e) {
            logger.debug("blackList for {} not exits", datasourceType.name());
        }
        return new CatalogerBlackList(new HashSet<>(blackList));

    }


    private CatalogerWhiteList getWhiteListFromProps() {
        List<String> whiteList = props.getStringList(CATALOGER_WHITE_LIST);
        return new CatalogerWhiteList(new HashSet<>(whiteList));
    }

    private CatalogerBlackList getBlackListFromProps() {
        List<String> blackList = props.getStringList(CATALOGER_BLACK_LIST);
        return new CatalogerBlackList(new HashSet<>(blackList));
    }

}
