package com.miotech.kun.datadiscovery.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceReq;
import com.miotech.kun.datadiscovery.model.vo.*;
import com.miotech.kun.datadiscovery.util.CollectionSolver;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.vo.*;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;
import com.miotech.kun.security.service.BaseSecurityService;
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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Service
@Slf4j
public class DataSourceAppService extends BaseSecurityService {
    @Value("${metadata.base-url:localhost:8084}")
    String url;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ConnectionAppService connectionAppService;

    public DataSourceBasicPage search(BasicSearchRequest basicSearchRequest) {
        PaginationVO<DataSourceBasicInfoVo> dbPages = this.searchDataSourceBasic(basicSearchRequest.getKeyword(), basicSearchRequest.getPageNumber(), basicSearchRequest.getPageSize());
        List<DataSourceBasicInfoVo> records = dbPages.getRecords();
        List<DatasourceBasicVO> collect = records.stream()
                .map(ds -> new DatasourceBasicVO(ds.getId(), ds.getName()))
                .collect(Collectors.toList());
        DataSourceBasicPage dataSourceBasicPage = new DataSourceBasicPage();
        dataSourceBasicPage.setDatasources(collect);
        dataSourceBasicPage.setPageSize(dbPages.getPageSize());
        dataSourceBasicPage.setPageNumber(dbPages.getPageNum());
        dataSourceBasicPage.setTotalCount(dbPages.getTotalCount());
        return dataSourceBasicPage;
    }

    @Deprecated
    public DataSourcePage search(DataSourceSearchRequest datasourceSearchRequest) {
        PaginationVO<DataSource> result = this.searchDataSource(datasourceSearchRequest.getSearch(),
                datasourceSearchRequest.getPageNumber(), datasourceSearchRequest.getPageSize());
        List<DataSource> records = result.getRecords();
        DataSourcePage dataSourcePage = new DataSourcePage(records.stream().map(this::convertToDataSourceVO).collect(Collectors.toList()));
        dataSourcePage.setTotalCount(result.getTotalCount());
        dataSourcePage.setPageNumber(datasourceSearchRequest.getPageNumber());
        dataSourcePage.setPageSize(datasourceSearchRequest.getPageSize());

        return dataSourcePage;
    }


    public List<DataSourceVO> list() {
        return listDataSource().stream().map(this::convertToDataSourceVO).collect(Collectors.toList());
    }


    @OperationRecord(type = OperationRecordType.DATASOURCE_ADD, args = {"#dataSourceReq"})
    public DataSourceVO add(DataSourceReq dataSourceReq) {
        checkUserConnection(dataSourceReq.getDatasourceConnection());
        fillCreateRequest(dataSourceReq);
        DataSourceBasicInfoVo dataSourceBasicInfo = this.createDataSource(convert(dataSourceReq));
        DatasourceConnectionVO datasourceConnection = dataSourceReq.getDatasourceConnection();
        List<ConnectionInfoSecurityVO> collect = connectionAppService.createDatasourceConnection(dataSourceBasicInfo.getId(), datasourceConnection);
        DatasourceConnectionVO datasourceConnectionVO = new DatasourceConnectionVO(collect);
        return convert(dataSourceBasicInfo, datasourceConnectionVO);
    }


    @OperationRecord(type = OperationRecordType.DATASOURCE_UPDATE, args = {"#id", "#dataSourceReq"})
    public DataSourceVO update(Long id, DataSourceReq dataSourceReq) {
        checkUserConnection(dataSourceReq.getDatasourceConnection());
        fillUpdateRequest(dataSourceReq);
        DataSourceBasicInfoVo dataSourceBasicInfoVo = this.updateDataSource(id, convert(dataSourceReq));
        DatasourceConnectionVO datasourceConnection = dataSourceReq.getDatasourceConnection();
        List<ConnectionInfoSecurityVO> collect = connectionAppService.updateDatasourceConnection(id, datasourceConnection);
        DatasourceConnectionVO datasourceConnectionVO = new DatasourceConnectionVO(collect);
        List<Long> newConnection = datasourceConnectionVO.getUserConnectionList().stream().map(ConnectionInfoSecurityVO::getId).collect(Collectors.toList());
        DataSource dataSource = findDataSourceById(id);
        List<ConnectionInfo> userConnectionList = dataSource.getDatasourceConnection().getUserConnectionList();
        List<Long> oldConnection = userConnectionList.stream().map(ConnectionInfo::getId).collect(Collectors.toList());
        CollectionSolver<Long> collectionSolver = new CollectionSolver<>(oldConnection, newConnection);
        Collection<Long> remove = collectionSolver.subtractRemove();
        remove.forEach(connId -> connectionAppService.deleteConnection(connId));
        return convert(dataSourceBasicInfoVo, datasourceConnectionVO);
    }

    private void checkUserConnection(DatasourceConnectionVO datasourceConnection) {
        if (CollectionUtils.isEmpty(datasourceConnection.getDatasourceConnectionList())) {
            throw new IllegalArgumentException("user connection  must size >0");
        }


    }


    @OperationRecord(type = OperationRecordType.DATASOURCE_DELETE, args = {"#id"})
    public void delete(Long id) {
        this.deleteDataSource(id);
    }


    public DataSourceVO findDataSource(Long id) {
        return convertToDataSourceVO(findDataSourceById(id));
    }


    private DataSourceBasicInfoRequest convert(DataSourceReq dataSourceReq) {
        return DataSourceBasicInfoRequest.newBuilder()
                .withName(dataSourceReq.getName())
                .withTags(dataSourceReq.getTags())
                .withDatasourceType(dataSourceReq.getDatasourceType())
                .withDatasourceConfigInfo(dataSourceReq.getDatasourceConfigInfo())
                .withCreateUser(dataSourceReq.getCreateUser())
                .withUpdateUser(dataSourceReq.getUpdateUser())
                .build();
    }


    private DataSourceVO convertToDataSourceVO(DataSource ds) {
        if (Objects.isNull(ds)) {
            return null;
        }
        return DataSourceVO.builder()
                .id(ds.getId())
                .datasourceType(ds.getDatasourceType().name())
                .name(ds.getName())
                .datasourceConnection(convert(ds.getDatasourceConnection()))
                .datasourceConfigInfo(ds.getDatasourceConfigInfo())
                .createUser(ds.getCreateUser())
                .createTime(ds.getCreateTime())
                .updateUser(ds.getUpdateUser())
                .updateTime(ds.getUpdateTime())
                .tags(ds.getTags())
                .build();
    }

    private DatasourceConnectionVO convert(DatasourceConnection datasourceConnection) {
        List<ConnectionInfo> datasourceConnectionList = datasourceConnection.getDatasourceConnectionList();
        List<ConnectionInfoSecurityVO> connectionInfoSecurityVOList = datasourceConnectionList.stream().map(connectionAppService::convertConnectionInfo).collect(Collectors.toList());
        return new DatasourceConnectionVO(connectionInfoSecurityVOList);
    }


    private DataSourceVO convert(DataSourceBasicInfoVo ds, DatasourceConnectionVO datasourceConnectionVO) {
        return DataSourceVO.builder()
                .id(ds.getId())
                .datasourceType(ds.getDatasourceType().name())
                .name(ds.getName())
                .datasourceConnection(datasourceConnectionVO)
                .datasourceConfigInfo(ds.getDatasourceConfigInfo())
                .createUser(ds.getCreateUser())
                .createTime(ds.getCreateTime())
                .updateUser(ds.getUpdateUser())
                .updateTime(ds.getUpdateTime())
                .tags(ds.getTags())
                .build();
    }


    private void fillCreateRequest(DataSourceReq dataSourceReq) {
        String username = getCurrentUsername();
        dataSourceReq.setCreateUser(username);
        dataSourceReq.setUpdateUser(username);
        OffsetDateTime now = OffsetDateTime.now();
        dataSourceReq.setCreateTime(now);
        dataSourceReq.setUpdateTime(now);
    }


    private void fillUpdateRequest(DataSourceReq dataSourceReq) {
        String username = getCurrentUsername();
        dataSourceReq.setUpdateUser(username);
        OffsetDateTime now = OffsetDateTime.now();
        dataSourceReq.setUpdateTime(now);
    }

    public List<ConnectionInfoSecurityVO> findDataSourceUserConnection(Long id) {
        DataSourceVO dataSource = findDataSource(id);
        if (Objects.isNull(dataSource)) {
            return Lists.newArrayList();
        }
        return dataSource.getDatasourceConnection().getUserConnectionList();
    }

    /************************************************* infra client reqeust*****************************************/


    @OperationRecord(type = OperationRecordType.DATASOURCE_PULL, args = {"#datasourceId"})
    public PullProcessVO pullDataSource(Long datasourceId) {
        String fullUrl = url + "/datasources/{id}/_pull";
        log.info("Request url : " + fullUrl);
        return restTemplate
                .postForEntity(fullUrl, null, PullProcessVO.class, datasourceId)
                .getBody();
    }


    private PaginationVO<DataSource> searchDataSource(String name, int pageNum, int pageSize) {
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

    private List<DataSource> listDataSource() {
        String searchUrl = url + "/datasources/list";
        ParameterizedTypeReference<List<DataSource>> typeRef = new ParameterizedTypeReference<List<DataSource>>() {
        };
        List<DataSource> body = restTemplate.exchange(searchUrl, HttpMethod.GET, null, typeRef).getBody();
        if (CollectionUtils.isEmpty(body)) {
            return Lists.newArrayList();
        }
        return body;
    }

    private PaginationVO<DataSourceBasicInfoVo> searchDataSourceBasic(String name, int pageNum, int pageSize) {
        String searchUrl = url + "/datasources/_search/basic";
        ParameterizedTypeReference<PaginationVO<DataSourceBasicInfoVo>> typeRef = new ParameterizedTypeReference<PaginationVO<DataSourceBasicInfoVo>>() {
        };
        HttpEntity httpEntity = new HttpEntity(DataSourceSearchFilter.newBuilder()
                .withName(name)
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .build());
        return restTemplate.exchange(searchUrl, HttpMethod.POST, httpEntity, typeRef).getBody();
    }

    private DataSourceBasicInfoVo createDataSource(DataSourceBasicInfoRequest request) {
        String createUrl = url + "/datasource";
        return restTemplate
                .exchange(createUrl, HttpMethod.POST, new HttpEntity(request), DataSourceBasicInfoVo.class)
                .getBody();
    }

    private DataSourceBasicInfoVo updateDataSource(Long id, DataSourceBasicInfoRequest request) {
        String updateUrl = url + "/datasource/{id}";
        return restTemplate
                .exchange(updateUrl, HttpMethod.PUT, new HttpEntity(request), DataSourceBasicInfoVo.class, id)
                .getBody();
    }

    private AcknowledgementVO deleteDataSource(Long id) {
        String createUrl = url + "/datasource/{id}";
        return restTemplate
                .exchange(createUrl, HttpMethod.DELETE, null, AcknowledgementVO.class, id)
                .getBody();
    }

    private DataSource findDataSourceById(Long id) {
        String createUrl = url + "/datasource/{id}";
        return restTemplate
                .exchange(createUrl, HttpMethod.GET, null, DataSource.class, id)
                .getBody();
    }

    private List<DatasourceTemplate> getDataSourceTypes() {
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


}
