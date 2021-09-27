package com.miotech.kun.metadata.common.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.common.service.gid.GidService;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestRequest;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestResponse;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetadataDatasetService implements MetadataServiceFacade {

    private final Logger logger = LoggerFactory.getLogger(MetadataDatasetService.class);

    private static final String DEFAULT_SUGGEST_DATASOURCE_TYPE = "AWS";

    private final MetadataDatasetDao metadataDatasetDao;
    private final DataSourceService dataSourceService;
    private final GidService gidService;

    @Inject
    public MetadataDatasetService(MetadataDatasetDao metadataDatasetDao,
                                  DataSourceService dataSourceService,
                                  GidService gidService) {
        this.metadataDatasetDao = metadataDatasetDao;
        this.dataSourceService = dataSourceService;
        this.gidService = gidService;
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

    public Dataset getDatasetByDatastore(DataStore datastore) {
        long gid = gidService.generate(datastore);
        logger.debug("fetched gid = {}", gid);

        Optional<Dataset> datasetOptional = fetchDatasetByGid(gid);
        if (datasetOptional.isPresent()) {
            return datasetOptional.get();
        } else {
            return null;
        }
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
}
