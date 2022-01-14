package com.miotech.kun.datadiscovery.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.*;
import com.miotech.kun.datadiscovery.model.entity.Watermark;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.model.vo.PullProcessVO;
import com.miotech.kun.metadata.core.model.dataset.DatabaseBaseInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/8/27
 */
@Service("DataDiscoveryMetadataService")
@Slf4j
public class MetadataService {

    @Value("${metadata.base-url:localhost:8084}")
    String url;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    GlossaryService glossaryService;

    public List<Database> getDatabases(DatabaseRequest request) {
        String fullUrl = url + "/dataset/databases";
        log.info("Request url : " + fullUrl);
        String params = buildFindDatabaseParams(request.getDataSourceIds());
        return restTemplate.exchange(fullUrl + params, HttpMethod.GET, null, new ParameterizedTypeReference<List<DatabaseBaseInfo>>() {
        }, params).getBody().stream().map(databaseBaseInfo -> {
            Database database = new Database();
            database.setName(databaseBaseInfo.getName());
            return database;
        }).collect(Collectors.toList());
    }

    private String buildFindDatabaseParams(List<Long> dataSourceIds) {
        if (CollectionUtils.isEmpty(dataSourceIds)) {
            return StringUtils.EMPTY;
        }

        return String.format("?dataSourceIds=%s", StringUtils.join(dataSourceIds, ","));
    }

    public DatasetBasicPage searchDatasets(BasicSearchRequest basicSearchRequest) {
        String suggestColumnUrl = url + "/dataset/search";
        DatasetBasicSearch datasetBasicSearch = restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(basicSearchRequest), DatasetBasicSearch.class).getBody();
        return buildDatasetBasicPage(datasetBasicSearch);
    }

    public DatasetBasicPage fullTextSearch(DatasetSearchRequest searchRequests) {
        String suggestColumnUrl = url + "/dataset/full-text/search";
        DatasetBasicSearch datasetBasicSearch = restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(searchRequests), DatasetBasicSearch.class).getBody();
        return buildDatasetBasicPage(datasetBasicSearch);
    }

    public Dataset findById(Long id) {
        String findByIdUrl = url + String.format("/dataset/%d", id);
        DatasetDetail datasetDetail = restTemplate.exchange(findByIdUrl, HttpMethod.GET, null, DatasetDetail.class).getBody();
        return convertFromDatasetDetail(datasetDetail);
    }

    public Dataset updateDataSet(Long id, DatasetRequest datasetRequest) {
        String updateDatasetUrl = url + String.format("/dataset/%d/update", id);
        DatasetDetail datasetDetail = restTemplate.exchange(updateDatasetUrl, HttpMethod.POST, new HttpEntity<>(datasetRequest), DatasetDetail.class).getBody();
        return convertFromDatasetDetail(datasetDetail);
    }

    public DatasetFieldPage findColumns(Long id, DatasetFieldSearchRequest searchRequest) {
        String findColumnsUrl = url + String.format("/dataset/%s/columns", id);
        DatasetFieldPageInfo datasetFieldPageInfo = restTemplate.exchange(findColumnsUrl, HttpMethod.POST, new HttpEntity<>(searchRequest), DatasetFieldPageInfo.class).getBody();
        DatasetFieldPage fieldPage = new DatasetFieldPage();
        for (DatasetFieldInfo column : datasetFieldPageInfo.getColumns()) {
            DatasetField datasetField = convertFromDatasetFieldInfo(column);
            fieldPage.add(datasetField);
        }

        fieldPage.setPageNumber(datasetFieldPageInfo.getPageNumber());
        fieldPage.setPageSize(datasetFieldPageInfo.getPageSize());
        fieldPage.setTotalCount(datasetFieldPageInfo.getTotalCount());
        return fieldPage;
    }

    public DatasetField updateColumn(Long id, DatasetFieldRequest datasetFieldRequest) {
        String updateColumnUrl = url + String.format("/dataset/%s/column/update", id);
        DatasetFieldInfo datasetFieldInfo = restTemplate.exchange(updateColumnUrl, HttpMethod.POST, new HttpEntity<>(datasetFieldRequest), DatasetFieldInfo.class).getBody();
        return convertFromDatasetFieldInfo(datasetFieldInfo);
    }

    public List<String> searchTags(String keyword) {
        String fullUrl = url + "/tags";
        log.info("Request url : " + fullUrl);
        Map<String, String> params = ImmutableMap.of("keyword", keyword);
        return restTemplate.exchange(fullUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
        }, params).getBody();
    }

    public PullProcessVO pullDataset(Long datasetId) {
        String fullUrl = url + "/datasets/{id}/_pull";
        log.info("Request url : " + fullUrl);
        return restTemplate
                .postForEntity(fullUrl, null, PullProcessVO.class, datasetId)
                .getBody();
    }

    public PullProcessVO pullDataSource(Long datasourceId) {
        String fullUrl = url + "/datasources/{id}/_pull";
        log.info("Request url : " + fullUrl);
        return restTemplate
                .postForEntity(fullUrl, null, PullProcessVO.class, datasourceId)
                .getBody();
    }

    public PaginationVO<DataSource> searchDataSource(String name, int pageNum, int pageSize) {
        String searchUrl = url + "/datasources/_search";
        ParameterizedTypeReference<PaginationVO<DataSource>> typeRef = new ParameterizedTypeReference<PaginationVO<DataSource>>() {
        };
        HttpEntity httpEntity = new HttpEntity(DataSourceSearchFilter.newBuilder()
                .withName(name)
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .build());
        return restTemplate.exchange(searchUrl, HttpMethod.POST, httpEntity, typeRef).getBody();
    }

    public DataSource createDataSource(DataSourceRequest request) {
        String createUrl = url + "/datasource";
        return restTemplate
                .exchange(createUrl, HttpMethod.POST, new HttpEntity(request), DataSource.class)
                .getBody();
    }

    public DataSource updateDataSource(Long id, DataSourceRequest request) {
        String updateUrl = url + "/datasource/{id}";
        return restTemplate
                .exchange(updateUrl, HttpMethod.PUT, new HttpEntity(request), DataSource.class, id)
                .getBody();
    }

    public AcknowledgementVO deleteDataSource(Long id) {
        String createUrl = url + "/datasource/{id}";
        return restTemplate
                .exchange(createUrl, HttpMethod.DELETE, null, AcknowledgementVO.class, id)
                .getBody();
    }

    public List<DatasourceTemplate> getDataSourceTypes() {
        String createUrl = url + "/datasource/types";
        return restTemplate.exchange(createUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<DatasourceTemplate>>() {
                }).getBody();
    }

    public Map<String, PullProcessVO> fetchLatestPullProcessByDataSourceIds(List<Long> datasourceIds) {
        String fullUrl = url + String.format("/datasources/_pull/latest?dataSourceIds=%s",
                StringUtils.join(datasourceIds.stream().map(Object::toString).collect(Collectors.toList()), ","));
        log.info("Request url : " + fullUrl);
        String json = restTemplate.getForObject(fullUrl, String.class);
        Map<String, PullProcessVO> result;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.readValue(json, new TypeReference<Map<String, PullProcessVO>>() {
            });
        } catch (Exception e) {
            log.error("Failed to converting json \"{}\" to map", json, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        return result;
    }

    public Optional<PullProcessVO> fetchLatestPullProcessForDataset(Long datasetId) {
        String fullUrl = url + "/datasets/{datasetId}/_pull/latest";
        log.info("Request url : " + fullUrl);
        return Optional.ofNullable(restTemplate.getForObject(fullUrl, PullProcessVO.class, datasetId));
    }

    public List<String> suggestDatabase(String prefix) {
        String suggestDatabaseUrl = url + "/dataset/database/_suggest";
        if (StringUtils.isNotBlank(prefix)) {
            suggestDatabaseUrl = suggestDatabaseUrl + "?prefix=" + prefix;
        }
        return Arrays.asList(restTemplate.getForEntity(suggestDatabaseUrl, String[].class).getBody());
    }

    public List<String> suggestTable(String databaseName, String prefix) {
        String suggestTableUrl = url + "/dataset/table/_suggest?databaseName=" + databaseName;
        if (StringUtils.isNotBlank(prefix)) {
            suggestTableUrl = suggestTableUrl + "&prefix=" + prefix;
        }
        return Arrays.asList(restTemplate.getForEntity(suggestTableUrl, String[].class).getBody());
    }

    public List<DatasetColumnSuggestResponse> suggestColumn(List<DatasetColumnSuggestRequest> columnSuggestRequests) {
        String suggestColumnUrl = url + "/dataset/column/_suggest";
        return Arrays.asList(restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(columnSuggestRequests), DatasetColumnSuggestResponse[].class).getBody());
    }

    private DatasetBasic convertFromBasicInfo(DatasetBasicInfo datasetBasicInfo) {
        DatasetBasic datasetBasic = new DatasetBasic();
        datasetBasic.setGid(datasetBasicInfo.getGid());
        datasetBasic.setName(datasetBasicInfo.getName());
        datasetBasic.setDatasource(datasetBasicInfo.getDatasource());
        datasetBasic.setDatabase(datasetBasicInfo.getDatabase());
        datasetBasic.setSchema(datasetBasicInfo.getSchema());
        datasetBasic.setDescription(datasetBasicInfo.getDescription());
        datasetBasic.setType(datasetBasicInfo.getType());
        if (datasetBasicInfo.getHighWatermark() != null) {
            datasetBasic.setHighWatermark(new Watermark(datasetBasicInfo.getHighWatermark().getTime()));
        }
        if (datasetBasicInfo.getLowWatermark() != null) {
            datasetBasic.setLowWatermark(new Watermark(datasetBasicInfo.getLowWatermark().getTime()));
        }
        datasetBasic.setOwners(datasetBasicInfo.getOwners());
        datasetBasic.setTags(datasetBasicInfo.getTags());
        datasetBasic.setDeleted(datasetBasicInfo.getDeleted());

        List<GlossaryBasic> glossaryBasics = glossaryService.getGlossariesByDataset(datasetBasicInfo.getGid());
        datasetBasic.setGlossaries(glossaryBasics);

        return datasetBasic;
    }

    private Dataset convertFromDatasetDetail(DatasetDetail datasetDetail) {
        Dataset dataset = new Dataset();
        dataset.setGid(datasetDetail.getGid());
        dataset.setName(datasetDetail.getName());
        dataset.setDatasource(datasetDetail.getDatasource());
        dataset.setDatabase(datasetDetail.getDatabase());
        dataset.setSchema(datasetDetail.getSchema());
        dataset.setDescription(datasetDetail.getDescription());
        dataset.setType(datasetDetail.getType());
        dataset.setHighWatermark(new Watermark(datasetDetail.getHighWatermark().getTime()));
        dataset.setLowWatermark(new Watermark(datasetDetail.getLowWatermark().getTime()));
        dataset.setOwners(datasetDetail.getOwners());
        dataset.setTags(datasetDetail.getTags());
        dataset.setDeleted(datasetDetail.getDeleted());

        dataset.setRowCount(datasetDetail.getRowCount());
        List<GlossaryBasic> glossaryBasics = glossaryService.getGlossariesByDataset(datasetDetail.getGid());
        dataset.setGlossaries(glossaryBasics);

        return dataset;
    }

    private DatasetField convertFromDatasetFieldInfo(DatasetFieldInfo datasetFieldInfo) {
        DatasetField datasetField = new DatasetField();
        datasetField.setId(datasetFieldInfo.getId());
        datasetField.setName(datasetFieldInfo.getName());
        datasetField.setType(datasetFieldInfo.getType());
        if (datasetFieldInfo.getHighWatermark() != null) {
            datasetField.setHighWatermark(new Watermark(datasetFieldInfo.getHighWatermark().getTime()));
        }
        datasetField.setDescription(datasetFieldInfo.getDescription());
        datasetField.setNotNullCount(datasetFieldInfo.getNotNullCount());
        datasetField.setNotNullPercentage(datasetFieldInfo.getNotNullPercentage());
        datasetField.setDistinctCount(datasetFieldInfo.getDistinctCount());
        return datasetField;
    }

    private DatasetBasicPage buildDatasetBasicPage(DatasetBasicSearch datasetBasicSearch) {
        List<DatasetBasic> datasetBasics = datasetBasicSearch.getDatasets().stream().map(basicSearch -> convertFromBasicInfo(basicSearch)).collect(Collectors.toList());
        DatasetBasicPage datasetBasicPage = new DatasetBasicPage(datasetBasics);
        datasetBasicPage.setPageNumber(datasetBasicSearch.getPageNumber());
        datasetBasicPage.setPageSize(datasetBasicSearch.getPageSize());
        datasetBasicPage.setTotalCount(datasetBasicSearch.getTotalCount());
        return datasetBasicPage;
    }

}
