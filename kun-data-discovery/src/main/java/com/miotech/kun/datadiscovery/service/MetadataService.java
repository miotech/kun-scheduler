package com.miotech.kun.datadiscovery.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.datadiscovery.constant.Constants;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.*;
import com.miotech.kun.datadiscovery.model.entity.Watermark;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.model.vo.PullProcessVO;
import com.miotech.kun.datadiscovery.util.convert.AppBasicConversionService;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.metadata.core.model.dataset.DatabaseBaseInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.*;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskInformation;
import com.miotech.kun.workflow.core.model.lineage.UpstreamTaskRequest;
import com.miotech.kun.workflow.core.model.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
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

    @Autowired
    private DeployedTaskFacade deployedTaskFacade;

    @Autowired
    private SearchAppService searchAppService;

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

    public DatasetBasicPage searchDatasets(BasicSearchRequest searchRequests) {
        UniversalSearchInfo universalSearchInfo = searchAppService.searchDataSet(searchRequests.getPageNumber(),
                searchRequests.getPageSize(), searchRequests.getKeyword());
        List<SearchedInfo> searchedInfoList = universalSearchInfo.getSearchedInfoList();
        DatasetBasicPage searchPage = new DatasetBasicPage();
        List<DatasetBasic> datasetBasicList = searchedInfoList.stream()
                .map(searchedInfo -> AppBasicConversionService.getSharedInstance().convert(searchedInfo, DatasetBasic.class))
                .collect(Collectors.toList());
        searchPage.setDatasets(datasetBasicList);
        searchPage.setPageSize(universalSearchInfo.getPageSize());
        searchPage.setPageSize(universalSearchInfo.getPageNumber());
        searchPage.setTotalCount(universalSearchInfo.getTotalCount());
        return searchPage;
    }

    public DatasetBasicPage fullTextSearch(BasicSearchRequest searchRequest) {
        UniversalSearchInfo universalSearchInfo = searchAppService.searchDataSet(searchRequest.getPageNumber(),
                searchRequest.getPageSize(), searchRequest.getKeyword());
        List<SearchedInfo> searchedInfoList = universalSearchInfo.getSearchedInfoList();
        DatasetBasicPage searchPage = new DatasetBasicPage();
        List<Long> gids = searchedInfoList.stream().map(SearchedInfo::getGid).collect(Collectors.toList());
        String upstreamTaskFetchUrl = url + "/lineage/datasets/upstream-task";
        List<UpstreamTaskInformation> upstreamTaskInformationList = restTemplate.exchange(upstreamTaskFetchUrl, HttpMethod.POST,
                new HttpEntity<>(new UpstreamTaskRequest(gids)), new ParameterizedTypeReference<List<UpstreamTaskInformation>>() {
                }).getBody();

        List<Long> taskIds = upstreamTaskInformationList.stream()
                .flatMap(taskInfo -> taskInfo.getTasks().stream())
                .map(Task::getId).collect(Collectors.toList());
        Map<Long, DeployedTask> deployedTaskMap = deployedTaskFacade.findByWorkflowTaskIds(taskIds);

        List<DatasetBasic> datasetBasics = searchedInfoList.stream()
                .map(searchedInfo -> convertFromSearchedInfo(searchedInfo, upstreamTaskInformationList, deployedTaskMap))
                .collect(Collectors.toList());
        searchPage.setDatasets(datasetBasics);
        searchPage.setPageNumber(universalSearchInfo.getPageNumber());
        searchPage.setPageSize(universalSearchInfo.getPageSize());
        searchPage.setTotalCount(universalSearchInfo.getTotalCount());
        return searchPage;
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

    private DatasetBasic convertFromSearchedInfo(SearchedInfo searchedInfo, List<UpstreamTaskInformation> upstreamTaskInformationList,
                                                 Map<Long, DeployedTask> deployedTaskMap) {
        DatasetBasic datasetBasic = AppBasicConversionService.getSharedInstance().convert(searchedInfo, DatasetBasic.class);
        List<GlossaryBasicInfo> glossaryBasics = glossaryService.getGlossariesByDataset(datasetBasic.getGid());
        datasetBasic.setGlossaries(glossaryBasics);
        List<UpstreamTask> upstreamTasks = getUpstreamTasks(upstreamTaskInformationList, deployedTaskMap, datasetBasic);
        datasetBasic.setUpstreamTasks(upstreamTasks);
        return datasetBasic;
    }

    private List<UpstreamTask> getUpstreamTasks(List<UpstreamTaskInformation> upstreamTaskInformationList, Map<Long, DeployedTask> deployedTaskMap, DatasetBasic datasetBasic) {
        Optional<UpstreamTaskInformation> upstreamTaskInformationOpt = upstreamTaskInformationList.stream()
                .filter(info -> info.getDatasetGid().equals(datasetBasic.getGid())).findFirst();
        if (upstreamTaskInformationOpt.isPresent()) {
            UpstreamTaskInformation upstreamTaskInformation = upstreamTaskInformationOpt.get();

            return upstreamTaskInformation.getTasks().stream()
                    .filter(task -> !task.getTags().contains(Constants.TAG_TYPE_MANUAL_RUN))
                    .map(taskInfo -> {
                        DeployedTask deployedTask = deployedTaskMap.get(taskInfo.getId());
                        Long definitionId = deployedTask == null ? null : deployedTask.getDefinitionId();
                        return new UpstreamTask(taskInfo.getId(), taskInfo.getName(), taskInfo.getDescription(), definitionId);
                    })
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
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
        List<GlossaryBasicInfo> glossaryBasics = glossaryService.getGlossariesByDataset(datasetDetail.getGid());
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

    public List<DatasetDetail> getDatasetDetailList(List<Long> datasetIdList) {
        String suggestColumnUrl = url + "/dataset/id_list";
        ParameterizedTypeReference<List<DatasetDetail>> typeRef = new ParameterizedTypeReference<List<DatasetDetail>>() {
        };
        ResponseEntity<List<DatasetDetail>> responseEntity = restTemplate.exchange(suggestColumnUrl, HttpMethod.POST, new HttpEntity<>(datasetIdList), typeRef);
        return responseEntity.getBody();
    }

}
