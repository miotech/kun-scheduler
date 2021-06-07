package com.miotech.kun.metadata.common.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetColumnHintRequest;
import com.miotech.kun.metadata.core.model.DatasetColumnHintResponse;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetadataDatasetService {
    private static final String DEFAULT_HINT_DATASOURCE_TYPE = "AWS";

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

    public List<String> hintDatabase(String prefix) {
        return hintDatabase(prefix, DEFAULT_HINT_DATASOURCE_TYPE);
    }

    public List<String> hintDatabase(String prefix, String dataSourceType) {
        List<Long> dataSourceIds = dataSourceService.fetchDataSourceIdByType(dataSourceType);
        if (CollectionUtils.isEmpty(dataSourceIds)) {
            return Lists.newArrayList();
        }

        Long dataSourceId = dataSourceIds.get(0);
        return metadataDatasetDao.hintDatabase(dataSourceId, prefix);
    }

    public List<String> hintTable(String databaseName, String prefix) {
        return hintTable(databaseName, prefix, DEFAULT_HINT_DATASOURCE_TYPE);
    }

    private List<String> hintTable(String databaseName, String prefix, String dataSourceType) {
        List<Long> dataSourceIds = dataSourceService.fetchDataSourceIdByType(dataSourceType);
        if (CollectionUtils.isEmpty(dataSourceIds)) {
            return Lists.newArrayList();
        }

        Long dataSourceId = dataSourceIds.get(0);
        return metadataDatasetDao.hintTable(dataSourceId, databaseName, prefix);
    }

    public List<DatasetColumnHintResponse> hintColumn(List<DatasetColumnHintRequest> columnHintRequests) {
        return hintColumn(columnHintRequests, DEFAULT_HINT_DATASOURCE_TYPE);
    }

    private List<DatasetColumnHintResponse> hintColumn(List<DatasetColumnHintRequest> columnHintRequests, String dataSourceType) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(columnHintRequests), "requests should not be empty");

        List<Long> dataSourceIds = dataSourceService.fetchDataSourceIdByType(dataSourceType);
        if (CollectionUtils.isEmpty(dataSourceIds)) {
            return Lists.newArrayList();
        }

        Long dataSourceId = dataSourceIds.get(0);
        return columnHintRequests.stream()
                .map(request ->
                        new DatasetColumnHintResponse(request.getDatabaseName(), request.getTableName(),
                                metadataDatasetDao.hintColumn(dataSourceId, request)))
                .collect(Collectors.toList());
    }
}
