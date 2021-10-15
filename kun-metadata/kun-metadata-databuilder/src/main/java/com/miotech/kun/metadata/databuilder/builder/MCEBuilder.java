package com.miotech.kun.metadata.databuilder.builder;

import com.amazonaws.services.glue.model.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.utils.HttpClientUtil;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.common.service.gid.GidService;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;
import com.miotech.kun.metadata.core.model.event.MetadataStatisticsEvent;
import com.miotech.kun.metadata.databuilder.client.GlueClient;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.constant.DatasetLifecycleStatus;
import com.miotech.kun.metadata.databuilder.extract.filter.HiveTableSchemaExtractFilter;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractor;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractorFactory;
import com.miotech.kun.metadata.databuilder.extract.tool.DataSourceBuilder;
import com.miotech.kun.metadata.databuilder.extract.tool.MetaStoreParseUtil;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.LoadSchemaResult;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.miotech.kun.metadata.databuilder.constant.OperatorKey.MSE_URL;

@Singleton
public class MCEBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MCEBuilder.class);

    private final Props props;
    private final DatabaseOperator operator;
    private final GidService gidService;
    private final MetadataDatasetDao datasetDao;
    private final DataSourceBuilder dataSourceBuilder;
    private final Loader loader;
    private final HttpClientUtil httpClientUtil;

    @Inject
    public MCEBuilder(Props props, DatabaseOperator operator, GidService gidService, MetadataDatasetDao datasetDao,
                      DataSourceBuilder dataSourceBuilder, Loader loader, HttpClientUtil httpClientUtil) {
        this.props = props;
        this.operator = operator;
        this.gidService = gidService;
        this.datasetDao = datasetDao;
        this.dataSourceBuilder = dataSourceBuilder;
        this.loader = loader;
        this.httpClientUtil = httpClientUtil;
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
            try {
                LoadSchemaResult loadSchemaResult = loader.loadSchema(datasetIterator.next());
                if (loadSchemaResult.getGid() < 0) {
                    continue;
                }

                // 发送消息
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

        DataSource dataSource = dataSourceBuilder.fetchByGid(gid);
        DatasetSchemaExtractor extractor = DatasetSchemaExtractorFactory.createExtractor(dataSource.getType());
        boolean existed = judgeDatasetExistence(dataset, dataSource, extractor, DatasetExistenceJudgeMode.DATASET);
        updateDatasetStatus(existed, gid);
        if (!existed) {
            return;
        }

        List<DatasetField> fields = extractor.extract(dataset.get(), dataSource);
        LoadSchemaResult loadSchemaResult = loader.loadSchema(gid, fields);

        // 发送消息
        try {
            MetadataStatisticsEvent mse = new MetadataStatisticsEvent(MetadataStatisticsEvent.EventType.FIELD, gid, loadSchemaResult.getSnapshotId());
            httpClientUtil.doPost(props.getString(MSE_URL), JSONUtils.toJsonString(mse));
        } catch (Exception e) {
            logger.warn("call mse execute api error: ", e);
        }

    }

    public void extractSchemaOfPush(String message) {
        MetadataChangeEvent mce = JSONUtils.jsonToObject(message, MetadataChangeEvent.class);

        String connectionInfo = operator.fetchOne("SELECT connection_info FROM kun_mt_datasource WHERE id = ?",
                rs -> rs.getString("connection_info"), mce.getDataSourceId());
        DataStore dataStore = generateDataStore(mce, connectionInfo);

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

    private DataStore generateDataStore(MetadataChangeEvent mce, String connectionInfo) {
        DataStore dataStore = operator.fetchOne("SELECT data_store FROM kun_mt_dataset WHERE datasource_id = ? AND database_name = ? AND name = ?",
                rs -> DataStoreJsonUtil.toDataStore(rs.getString("data_store")), mce.getDataSourceId(), mce.getDatabaseName(), mce.getTableName());
        if (dataStore != null) {
            return dataStore;
        }

        if (mce.getDataSourceType().equals(MetadataChangeEvent.DataSourceType.GLUE)) {
            AWSDataSource awsDataSource = JSONUtils.jsonToObject(connectionInfo, AWSDataSource.class);
            Table table = GlueClient.searchTable(awsDataSource, mce.getDatabaseName(), mce.getTableName());
            if (table == null) {
                throw new NoSuchElementException(String.format("Extract task terminated, table: %s does not existed", mce.getTableName()));
            }

            if (!HiveTableSchemaExtractFilter.filter(table.getTableType())) {
                throw new UnsupportedOperationException(String.format("Extract task terminated, table type: %s not support", table.getTableType()));
            }

            return new HiveTableStore(table.getStorageDescriptor().getLocation(), mce.getDatabaseName(), mce.getTableName());
        } else if (mce.getDataSourceType().equals(MetadataChangeEvent.DataSourceType.HIVE)) {
            String location = MetaStoreParseUtil.parseLocation(MetaStoreParseUtil.Type.META_STORE_CLIENT, dataSourceBuilder.fetchById(mce.getDataSourceId()),
                    mce.getDatabaseName(), mce.getTableName());
            return new HiveTableStore(location, mce.getDatabaseName(), mce.getTableName());
        } else {
            throw new UnsupportedOperationException("Unsupported dataSourceType: " + mce.getDataSourceType().name());
        }
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
            int updateRowCount = operator.update("UPDATE kun_mt_dataset SET deleted = false WHERE gid = ? AND deleted is true", gid);
            if (updateRowCount == 1) {
                // 记录MANAGED事件
                operator.update("INSERT INTO kun_mt_dataset_lifecycle(dataset_gid, status, create_at) VALUES(?, ?, ?)", gid, DatasetLifecycleStatus.MANAGED.name(), DateTimeUtils.now());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Dataset: {} no longer existed, marked as `deleted`", gid);
            }

            int updateRowCount = operator.update("UPDATE kun_mt_dataset SET deleted = true WHERE gid = ? AND deleted is false", gid);
            if (updateRowCount == 1) {
                // 记录DELETED事件
                operator.update("INSERT INTO kun_mt_dataset_lifecycle(dataset_gid, status, create_at) VALUES(?, ?, ?)", gid, DatasetLifecycleStatus.DELETED.name(), DateTimeUtils.now());
            }
        }
    }

}
