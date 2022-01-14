package com.miotech.kun.metadata.common.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DatabaseBaseInfo;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.vo.*;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetadataDatasetService implements MetadataServiceFacade {

    private final Logger logger = LoggerFactory.getLogger(MetadataDatasetService.class);

    private static final String DEFAULT_SUGGEST_DATASOURCE_TYPE = "HIVE";

    private final MetadataDatasetDao metadataDatasetDao;
    private final DataSourceService dataSourceService;

    @Inject
    public MetadataDatasetService(MetadataDatasetDao metadataDatasetDao,
                                  DataSourceService dataSourceService) {
        this.metadataDatasetDao = metadataDatasetDao;
        this.dataSourceService = dataSourceService;
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
        logger.debug("fetching datasource by dataStore = {}",JSONUtils.toJsonString(dataStore));
        Long dataSourceId = getDataSourceIdByDatastore(dataStore);
        if(dataSourceId == null){
            throw new IllegalStateException("datasource not exist with datastore = " + JSONUtils.toJsonString(dataStore));
        }
        String locationInfo = dataStore.getLocationInfo();
        String dsi = dataSourceId + ":" + locationInfo;
        logger.debug("fetching dataset by dsi = {}",dsi);
        Dataset dataset = metadataDatasetDao.fetchDatasetByDSI(dsi);
        if (dataset != null) {
            return dataset;
        } else {
            logger.debug("dataset with dsi = {} not exist,going to create dataset",dsi);
            return createDataSet(dataSourceId, dataStore);
        }
    }

    public Dataset createDataSet(Dataset dataset) {
        return metadataDatasetDao.createDataset(dataset);
    }

    public Dataset fetchDataSetByDSI(String dsi) {
        return metadataDatasetDao.fetchDatasetByDSI(dsi);
    }

    private Long getDataSourceIdByDatastore(DataStore dataStore) {
        ConnectionInfo connectionInfo = dataStore.getConnectionInfo();
        Long dataSourceId = dataSourceService.getDataSourceIdByConnectionInfo(dataStore.getType(), connectionInfo);
        return dataSourceId;
    }


    public List<String> suggestTable(String databaseName, String prefix) {
        return suggestTable(databaseName, prefix, DEFAULT_SUGGEST_DATASOURCE_TYPE);
    }

    private Dataset createDataSet(Long dataSourceId, DataStore dataStore) {
        Dataset dataset = Dataset.newBuilder()
                .withName(dataStore.getName())
                .withDatasourceId(dataSourceId)
                .withDataStore(dataStore)
                .build();
        return metadataDatasetDao.createDataset(dataset);
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

    public DatasetBasicSearch searchDatasets(BasicSearchRequest request) {
        return metadataDatasetDao.searchDatasets(request);
    }

    public DatasetBasicSearch fullTextSearch(DatasetSearchRequest request) {
        return metadataDatasetDao.fullTextSearch(request);
    }

    public DatasetDetail getDatasetDetail(Long id) {
        return metadataDatasetDao.getDatasetDetail(id);
    }

    public void updateDataset(Long id, DatasetUpdateRequest updateRequest) {
        metadataDatasetDao.updateDataset(id, updateRequest);
    }

    public DatasetFieldPageInfo searchDatasetFields(Long id, DatasetColumnSearchRequest searchRequest) {
        return metadataDatasetDao.searchDatasetFields(id, searchRequest);
    }

    public DatasetFieldInfo updateDatasetColumn(Long id, DatasetColumnUpdateRequest updateRequest) {
        return metadataDatasetDao.updateDatasetColumn(id, updateRequest);
    }
}
