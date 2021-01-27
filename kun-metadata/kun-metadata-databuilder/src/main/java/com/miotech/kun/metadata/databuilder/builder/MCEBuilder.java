package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.mce.MetadataChangeEvent;
import com.miotech.kun.metadata.core.model.mce.MetadataStatisticsEvent;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractor;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractorFactory;
import com.miotech.kun.metadata.databuilder.extract.tool.DataSourceBuilder;
import com.miotech.kun.metadata.databuilder.extract.tool.KafkaUtil;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.BROKERS;
import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.MSE_TOPIC;

@Singleton
public class MCEBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MCEBuilder.class);

    private final Props props;
    private final DatabaseOperator operator;
    private final GidService gidService;
    private final MetadataDatasetDao datasetDao;
    private final DataSourceBuilder dataSourceBuilder;
    private final Loader loader;

    @Inject
    public MCEBuilder(Props props, DatabaseOperator operator, GidService gidService, MetadataDatasetDao datasetDao,
                      DataSourceBuilder dataSourceBuilder, Loader loader) {
        this.props = props;
        this.operator = operator;
        this.gidService = gidService;
        this.datasetDao = datasetDao;
        this.dataSourceBuilder = dataSourceBuilder;
        this.loader = loader;
    }

    public void extractSchemaOfDataSource(Long datasourceId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin to extractSchemaOfDataSource, datasourceId: {}", datasourceId);
        }
        DataSource dataSource = dataSourceBuilder.fetchById(datasourceId);
        DatasetSchemaExtractor extractor = DatasetSchemaExtractorFactory.createExtractor(dataSource.getType());
        List<Long> gids = operator.fetchAll("SELECT gid FROM kun_mt_dataset WHERE datasource_id = ?",
                rs -> rs.getLong("gid"), datasourceId);
        for (Long gid : gids) {
            try {
                boolean existed = judgeDatasetExistence(gid, dataSource, extractor, DatasetExistenceJudgeMode.DATASOURCE);
                updateDatasetStatus(existed, gid);
            } catch (Exception e) {
                logger.error("Extract schema error: ", e);
                logger.error("gid: {}", gid);
            }
        }

        Iterator<Dataset> datasetIterator = extractor.extract(dataSource);
        while (datasetIterator.hasNext()) {
            long gid = loader.loadSchema(datasetIterator.next());
            if (gid < 0) {
                continue;
            }

            // 发送消息
            /*MetadataStatisticsEvent mse = new MetadataStatisticsEvent(MetadataStatisticsEvent.EventType.TABLE, gid);
            KafkaUtil.send(props.getString(BROKERS), props.getString(MSE_TOPIC), JSONUtils.toJsonString(mse));*/
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

        DataSource dataSource = dataSourceBuilder.fetchByGid(gid);
        DatasetSchemaExtractor extractor = DatasetSchemaExtractorFactory.createExtractor(dataSource.getType());
        boolean existed = judgeDatasetExistence(dataset, dataSource, extractor, DatasetExistenceJudgeMode.DATASET);
        updateDatasetStatus(existed, gid);
        if (!existed) {
            return;
        }

        List<DatasetField> fields = extractor.extract(dataset.get(), dataSource);
        loader.loadSchema(gid, fields);

        // 发送消息
        try {
            MetadataStatisticsEvent mse = new MetadataStatisticsEvent(MetadataStatisticsEvent.EventType.FIELD, gid);
            KafkaUtil.send(props.getString(BROKERS), props.getString(MSE_TOPIC), JSONUtils.toJsonString(mse));
        } catch (Exception e) {
            logger.warn("send mse message error: ", e);
        }

    }

    public void extractSchemaOfPush(String message) {
        MetadataChangeEvent mce = JSONUtils.jsonToObject(message, MetadataChangeEvent.class);

        String connectionInfo = operator.fetchOne("SELECT connection_info FROM kun_mt_datasource WHERE id = ?",
                rs -> rs.getString("connection_info"), mce.getDataSourceId());
        AWSDataSource awsDataSource = JSONUtils.jsonToObject(connectionInfo, AWSDataSource.class);
        DataStore dataStore = new HiveTableStore(awsDataSource.getAthenaUrl(), mce.getDatabaseName(), mce.getTableName());

        // 生成gid
        long gid = gidService.generate(dataStore);
        if (mce.getEventType().equals(MetadataChangeEvent.EventType.DROP_TABLE)) {
            updateDatasetStatus(false, gid);
            return;
        }

        Optional<Dataset> dataset = datasetDao.fetchDatasetByGid(gid);
        if (!dataset.isPresent()) {
            operator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES(?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                    gid, mce.getTableName(),
                    mce.getDataSourceId(),
                    DataStoreJsonUtil.toJson(dataStore),
                    mce.getDatabaseName(),
                    dataStore.getDSI().toFullString(),
                    false
            );
        }

        extractSchemaOfDataset(gid);
    }

    private boolean judgeDatasetExistence(long gid, DataSource dataSource, DatasetSchemaExtractor extractor, DatasetExistenceJudgeMode judgeMode) {
        Optional<Dataset> dataset = datasetDao.fetchDatasetByGid(gid);
        if (!dataset.isPresent()) {
            logger.warn("Dataset not found, gid: {}", gid);
            return false;
        }

        return judgeDatasetExistence(dataset, dataSource, extractor, judgeMode);
    }

    private boolean judgeDatasetExistence(Optional<Dataset> dataset, DataSource dataSource, DatasetSchemaExtractor extractor, DatasetExistenceJudgeMode judgeMode) {
        if (!dataset.isPresent()) {
            return false;
        }

        return extractor.judgeExistence(dataset.get(), dataSource, judgeMode);
    }

    private void updateDatasetStatus(boolean existed, long gid) {
        if (existed) {
            operator.update("UPDATE kun_mt_dataset SET deleted = false WHERE gid = ?", gid);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Dataset: {} no longer existed, marked as `deleted`", gid);
            }

            operator.update("UPDATE kun_mt_dataset SET deleted = true WHERE gid = ?", gid);
        }
    }

}
