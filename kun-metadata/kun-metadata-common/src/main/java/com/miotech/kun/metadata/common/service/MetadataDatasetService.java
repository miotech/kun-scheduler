package com.miotech.kun.metadata.common.service;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.common.utils.MetadataTypeConvertor;
import com.miotech.kun.metadata.core.model.constant.DatasetLifecycleStatus;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.dataset.DatabaseBaseInfo;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.*;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetadataDatasetService implements MetadataServiceFacade {

    private final Logger logger = LoggerFactory.getLogger(MetadataDatasetService.class);

    private static final String DEFAULT_SUGGEST_DATASOURCE_TYPE = "HIVE";

    private final MetadataDatasetDao metadataDatasetDao;
    private final DataSourceService dataSourceService;
    private final SearchService searchService;

    @Inject
    public MetadataDatasetService(MetadataDatasetDao metadataDatasetDao,
                                  DataSourceService dataSourceService,
                                  SearchService searchService
    ) {
        this.metadataDatasetDao = metadataDatasetDao;
        this.dataSourceService = dataSourceService;
        this.searchService = searchService;
    }

    /**
     * Fetch dataset by its global id and returns an optional object.
     *
     * @param gid global id
     * @return An optional Dataset object. Not present if not found.
     */
    public Optional<Dataset> fetchDatasetByGid(Long gid) {
        Preconditions.checkNotNull(gid, "Argument `gid` cannot be null");
        return metadataDatasetDao.fetchDatasetByGid(gid);
    }

    /**
     * @param gid
     * @return a dataset not contains any static info
     */
    public Dataset fetBasicDatasetByGid(Long gid) {
        Preconditions.checkNotNull(gid, "Argument `gid` cannot be null");
        return metadataDatasetDao.fetchBasicDatasetByGid(gid);
    }

    /**
     * @param datasetIds
     * @return a dataset list not contains any static info
     */
    public List<Dataset> fetchBasicDatasetList(List<Long> datasetIds) {
        Preconditions.checkNotNull(datasetIds, "Argument `dataset` cannot be null");
        return metadataDatasetDao.fetchBasicDatasetByGids(datasetIds);
    }


    public List<String> suggestDatabase(String prefix) {
        return suggestDatabase(prefix, DEFAULT_SUGGEST_DATASOURCE_TYPE);
    }

    public List<String> suggestDatabase(String prefix, String dataSourceType) {
        List<Long> dataSourceIds = dataSourceService.fetchDataSourceIdByType(dataSourceType);
        if (CollectionUtils.isEmpty(dataSourceIds)) {
            return Lists.newArrayList();
        }

        Long dataSourceId = dataSourceIds.get(0);
        return metadataDatasetDao.suggestDatabase(dataSourceId, prefix);
    }

    public Dataset createDataSetIfNotExist(DataStore dataStore) {
        logger.debug("fetching datasource by dataStore = {}", JSONUtils.toJsonString(dataStore));
        Long dataSourceId = getDataSourceIdByDatastore(dataStore);
        if (dataSourceId == null) {
            throw new IllegalStateException("datasource not exist with datastore = " + JSONUtils.toJsonString(dataStore));
        }
        Dataset dataset = Dataset.newBuilder()
                .withName(dataStore.getName())
                .withDatasourceId(dataSourceId)
                .withDataStore(dataStore)
                .build();
        return createDataSet(dataset);

    }

    public Dataset createDataSet(Dataset datasetInsert) {
        Dataset dataset = metadataDatasetDao.createDataset(datasetInsert);
        updateSearchDataSetInfo(dataset.getGid(), dataset.isDeleted());
        return dataset;
    }

    public Dataset fetchDataSetByDSI(String dsi) {
        return metadataDatasetDao.fetchDatasetByDSI(dsi);
    }

    private Long getDataSourceIdByDatastore(DataStore dataStore) {
        DatasourceType datasourceType = MetadataTypeConvertor.covertStoreTypeToSourceType(dataStore.getType());
        return dataSourceService.fetchDataSourceByConnectionInfo(datasourceType, dataStore.getConnectionConfigInfo());
    }


    public List<String> suggestTable(String databaseName, String prefix) {
        return suggestTable(databaseName, prefix, DEFAULT_SUGGEST_DATASOURCE_TYPE);
    }


    private List<String> suggestTable(String databaseName, String prefix, String dataSourceType) {
        List<Long> dataSourceIds = dataSourceService.fetchDataSourceIdByType(dataSourceType);
        if (CollectionUtils.isEmpty(dataSourceIds)) {
            return Lists.newArrayList();
        }

        Long dataSourceId = dataSourceIds.get(0);
        return metadataDatasetDao.suggestTable(dataSourceId, databaseName, prefix);
    }

    public List<DatasetColumnSuggestResponse> suggestColumn(List<DatasetColumnSuggestRequest> columnSuggestRequests) {
        return suggestColumn(columnSuggestRequests, DEFAULT_SUGGEST_DATASOURCE_TYPE);
    }

    private List<DatasetColumnSuggestResponse> suggestColumn(List<DatasetColumnSuggestRequest> columnSuggestRequests, String dataSourceType) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(columnSuggestRequests), "requests should not be empty");

        List<Long> dataSourceIds = dataSourceService.fetchDataSourceIdByType(dataSourceType);
        if (CollectionUtils.isEmpty(dataSourceIds)) {
            return Lists.newArrayList();
        }

        Long dataSourceId = dataSourceIds.get(0);
        return columnSuggestRequests.stream()
                .map(request ->
                        new DatasetColumnSuggestResponse(request.getDatabaseName(), request.getTableName(),
                                metadataDatasetDao.suggestColumn(dataSourceId, request)))
                .collect(Collectors.toList());
    }

    public List<DatabaseBaseInfo> getDatabases(List<Long> dataSourceIds) {
        return metadataDatasetDao.getDatabases(dataSourceIds);
    }


    public DatasetDetail getDatasetDetail(Long id) {
        return metadataDatasetDao.getDatasetDetail(id);
    }

    public List<DatasetDetail> getDatasetDetailList(List<Long> idList) {
        return metadataDatasetDao.getDatasetDetailList(idList);
    }

    public void updateDataset(Long id, DatasetUpdateRequest updateRequest) {
        metadataDatasetDao.updateDataset(id, updateRequest);
        updateSearchDataSetInfo(id, false);
    }

    public DatasetFieldPageInfo searchDatasetFields(Long id, DatasetColumnSearchRequest searchRequest) {
        return metadataDatasetDao.searchDatasetFields(id, searchRequest);
    }

    public DatasetFieldInfo updateDatasetColumn(Long id, DatasetColumnUpdateRequest updateRequest) {
        return metadataDatasetDao.updateDatasetColumn(id, updateRequest);
    }

    private void updateSearchDataSetInfo(final Long gid, final Boolean deleted) {
        logger.debug("update SearchDataSetInfo gid:{},deleted:{}", gid, deleted);
        if (deleted) {
            SearchedInfo searchedInfoRemove = SearchedInfo.Builder.newBuilder()
                    .withGid(gid)
                    .withResourceType(ResourceType.DATASET)
                    .build();
            searchService.remove(searchedInfoRemove);
            return;
        }
        DatasetDetail datasetDetail = getDatasetDetail(gid);
        logger.debug("update,gid:{}: datasetDetail:{}", gid, datasetDetail);
        if (Objects.nonNull(datasetDetail)) {
            DataSetResourceAttribute resourceAttribute = DataSetResourceAttribute.Builder
                    .newBuilder()
                    .withDatasource(datasetDetail.getDatasource())
                    .withDatabase(datasetDetail.getDatabase())
                    .withSchema(datasetDetail.getSchema())
                    .withType(datasetDetail.getType())
                    .withOwners(Joiner.on(",").join(datasetDetail.getOwners()))
                    .withTags(Joiner.on(",").join(datasetDetail.getTags()))
                    .build();
            SearchedInfo searchedInfoUpdate = SearchedInfo.Builder.newBuilder()
                    .withGid(datasetDetail.getGid())
                    .withResourceType(ResourceType.DATASET)
                    .withName(datasetDetail.getName())
                    .withDescription(datasetDetail.getDescription())
                    .withResourceAttribute(resourceAttribute)
                    .withDeleted(datasetDetail.getDeleted())
                    .build();
            searchService.saveOrUpdate(searchedInfoUpdate);
        }

    }

    public void updateDatasetStatus(boolean accept, long gid) {
        if (!accept) {
            if (logger.isDebugEnabled()) {
                logger.debug("Dataset: {} no longer existed, marked as `deleted`", gid);
            }
            int updateRowCount = metadataDatasetDao.updateStatus(gid, true);
            updateSearchDataSetInfo(gid, true);
            if (updateRowCount == 1) {
                metadataDatasetDao.recordLifecycle(gid, DatasetLifecycleStatus.DELETED);
            }
            return;
        }
        int updateRowCount = metadataDatasetDao.updateStatus(gid, false);
        updateSearchDataSetInfo(gid, false);
        if (updateRowCount == 1) {
            metadataDatasetDao.recordLifecycle(gid, DatasetLifecycleStatus.MANAGED);
        }
    }


    public List<Dataset> getListByDatasource(Long datasourceId) {
        return metadataDatasetDao.getListByDatasource(datasourceId);
    }

    public Optional<Dataset> fetchDataSet(Long datasourceId, String databaseName, String name) {
        return metadataDatasetDao.fetchDataSet(datasourceId, databaseName, name);
    }
}
