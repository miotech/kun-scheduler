package com.miotech.kun.datadiscovery.testing;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceReq;
import com.miotech.kun.datadiscovery.model.bo.DataSourceSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.SecurityInfo;
import com.miotech.kun.datadiscovery.model.enums.ConnectionRole;
import com.miotech.kun.datadiscovery.model.enums.SecurityModule;
import com.miotech.kun.datadiscovery.model.vo.*;
import com.miotech.kun.datadiscovery.service.ConnectionAppService;
import com.miotech.kun.datadiscovery.service.DataSourceAppService;
import com.miotech.kun.datadiscovery.testing.mockdata.MockConnectionFactory;
import com.miotech.kun.datadiscovery.testing.mockdata.MockDataSourceFactory;
import com.miotech.kun.datadiscovery.util.convert.AppBasicConversionService;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoVo;
import com.miotech.kun.metadata.core.model.vo.PaginationVO;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class DataSourceAppServiceTest extends DataDiscoveryTestBase {
    @Autowired
    private DataSourceAppService dataSourceAppService;

    @MockBean
    private ConnectionAppService connectionAppService;

    @Test
    void test_search_basic() {
        BasicSearchRequest request = new BasicSearchRequest();
        request.setKeyword("test");
        DataSourceBasicInfoVo test_search = DataSourceBasicInfoVo.newBuilder()
                .withName("test_search")
                .withDatasourceType(DatasourceType.POSTGRESQL)
                .withId(1L)
                .build();
        ArrayList<DataSourceBasicInfoVo> dataSourceBasicInfoVos = Lists.newArrayList(test_search);
        PaginationVO<DataSourceBasicInfoVo> mockPage = new PaginationVO<>(1, 1, 1, dataSourceBasicInfoVos);
        String searchUrl = url + "/datasources/_search/basic";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(searchUrl), Mockito.eq(HttpMethod.POST), any(), Mockito.<ParameterizedTypeReference<PaginationVO<DataSourceBasicInfoVo>>>any()))
                .willReturn(new ResponseEntity<>(mockPage, HttpStatus.OK));
        DataSourceBasicPage search = dataSourceAppService.search(request);
        assertThat(search.getPageSize(), is(mockPage.getPageSize()));
        assertThat(search.getPageNumber(), is(mockPage.getPageNum()));
        assertThat(search.getTotalCount(), is(mockPage.getTotalCount()));
        assertThat(search.getDatasources().size(), is(mockPage.getRecords().size()));
        assertThat(search.getDatasources().get(0).getId(), is(mockPage.getRecords().get(0).getId()));
    }

    @Test
    void test_search_datasource() {
        DataSourceSearchRequest request = new DataSourceSearchRequest();
        request.setSearch("test");
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 5432);
        DatasourceConnection datasourceConnection = MockConnectionFactory.createDatasourceConnection(datasourceId, pgConnectionConfigInfo);
        DataSource test_search = MockDataSourceFactory.createDataSource(datasourceId, "test_search", hostPortDatasourceConfig, datasourceConnection, DatasourceType.POSTGRESQL);
        ArrayList<DataSource> dataSources = Lists.newArrayList(test_search);
        PaginationVO<DataSource> mockPage = new PaginationVO<>(request.getPageNumber(), request.getPageSize(), 1, dataSources);
        String searchUrl = url + "/datasources/_search";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(searchUrl), Mockito.eq(HttpMethod.POST), any(), Mockito.<ParameterizedTypeReference<PaginationVO<DataSource>>>any()))
                .willReturn(new ResponseEntity<>(mockPage, HttpStatus.OK));
        when(connectionAppService.convertConnectionInfo(Mockito.argThat(argument -> argument.getConnScope().equals(ConnScope.USER_CONN)))).thenReturn(mockConnectionInfoSecurityVO(datasourceConnection.getUserConnectionList().get(0)));
        when(connectionAppService.convertConnectionInfo(Mockito.argThat(argument -> argument.getConnScope().equals(ConnScope.DATA_CONN)))).thenReturn(mockConnectionInfoSecurityVO(datasourceConnection.getDataConnection()));
        when(connectionAppService.convertConnectionInfo(Mockito.argThat(argument -> argument.getConnScope().equals(ConnScope.METADATA_CONN)))).thenReturn(mockConnectionInfoSecurityVO(datasourceConnection.getMetadataConnection()));
        when(connectionAppService.convertConnectionInfo(Mockito.argThat(argument -> argument.getConnScope().equals(ConnScope.STORAGE_CONN)))).thenReturn(mockConnectionInfoSecurityVO(datasourceConnection.getStorageConnection()));
        DataSourcePage search = dataSourceAppService.search(request);
        assertThat(search.getPageSize(), is(mockPage.getPageSize()));
        assertThat(search.getPageNumber(), is(mockPage.getPageNum()));
        assertThat(search.getTotalCount(), is(mockPage.getTotalCount()));
        assertThat(search.getDatasources().size(), is(mockPage.getRecords().size()));
        assertThat(search.getDatasources().get(0).getId(), is(mockPage.getRecords().get(0).getId()));
        assertThat(search.getDatasources().get(0).getDatasourceType(), is(DatasourceType.POSTGRESQL.name()));
        assertThat(search.getDatasources().get(0).getDatasourceConnection().getUserConnectionList().size(), is(1));
        assertThat(search.getDatasources().get(0).getDatasourceConnection().getDataConnection(), is(notNullValue()));
        assertThat(search.getDatasources().get(0).getDatasourceConnection().getMetadataConnection(), is(notNullValue()));
        assertThat(search.getDatasources().get(0).getDatasourceConnection().getStorageConnection(), is(notNullValue()));
        assertThat(search.getDatasources().get(0).getDatasourceConnection().getStorageConnection(), is(notNullValue()));
    }

    @Test
    void test_list() {
        Long datasourceId = 1L;
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 5432);
        DatasourceConnection datasourceConnection = MockConnectionFactory.createDatasourceConnection(datasourceId, pgConnectionConfigInfo);
        DataSource dataSource = MockDataSourceFactory.createDataSource(datasourceId, "list", hostPortDatasourceConfig, datasourceConnection, DatasourceType.POSTGRESQL);
        ArrayList<DataSource> dataSources = Lists.newArrayList(dataSource);
        String searchUrl = url + "/datasources/list";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(searchUrl), Mockito.eq(HttpMethod.GET), any(), Mockito.<ParameterizedTypeReference<List<DataSource>>>any()))
                .willReturn(new ResponseEntity<>(dataSources, HttpStatus.OK));
        when(connectionAppService.convertConnectionInfo(Mockito.argThat(argument -> argument.getConnScope().equals(ConnScope.USER_CONN)))).thenReturn(mockConnectionInfoSecurityVO(datasourceConnection.getUserConnectionList().get(0)));
        when(connectionAppService.convertConnectionInfo(Mockito.argThat(argument -> argument.getConnScope().equals(ConnScope.DATA_CONN)))).thenReturn(mockConnectionInfoSecurityVO(datasourceConnection.getDataConnection()));
        when(connectionAppService.convertConnectionInfo(Mockito.argThat(argument -> argument.getConnScope().equals(ConnScope.METADATA_CONN)))).thenReturn(mockConnectionInfoSecurityVO(datasourceConnection.getMetadataConnection()));
        when(connectionAppService.convertConnectionInfo(Mockito.argThat(argument -> argument.getConnScope().equals(ConnScope.STORAGE_CONN)))).thenReturn(mockConnectionInfoSecurityVO(datasourceConnection.getStorageConnection()));
        List<DataSourceVO> list = dataSourceAppService.list();
        DataSourceVO dataSourceVO = list.get(0);
        assertThat(dataSourceVO.getId(), is(datasourceId));
        assertThat(dataSourceVO.getDatasourceType(), is(DatasourceType.POSTGRESQL.name()));
        assertThat(dataSourceVO.getDatasourceConnection().getUserConnectionList().size(), is(1));
        assertThat(dataSourceVO.getDatasourceConnection().getDataConnection(), is(notNullValue()));
        assertThat(dataSourceVO.getDatasourceConnection().getMetadataConnection(), is(notNullValue()));
        assertThat(dataSourceVO.getDatasourceConnection().getStorageConnection(), is(notNullValue()));
        assertThat(dataSourceVO.getDatasourceConnection().getStorageConnection(), is(notNullValue()));
    }

    @Test
    void test_add() {
        DataSourceReq dataSourceReq = new DataSourceReq();
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 5432);
        dataSourceReq.setDatasourceConfigInfo(hostPortDatasourceConfig);
        dataSourceReq.setName("test_datasource");
        dataSourceReq.setDatasourceType(DatasourceType.POSTGRESQL);
        dataSourceReq.setTags(ImmutableList.of("test1", "test2"));
        dataSourceReq.setCreateTime(DateTimeUtils.now());
        dataSourceReq.setCreateUser("users");
        DatasourceConnectionVO datasourceConnectionVO = new DatasourceConnectionVO();
        ConnectionInfoSecurityVO user_connection = getConnectionInfoSecurityVO(ConnScope.USER_CONN);
        datasourceConnectionVO.setUserConnectionList(ImmutableList.of(user_connection));
        datasourceConnectionVO.setDataConnection(getConnectionInfoSecurityVO(ConnScope.DATA_CONN));
        datasourceConnectionVO.setMetadataConnection(getConnectionInfoSecurityVO(ConnScope.METADATA_CONN));
        datasourceConnectionVO.setStorageConnection(getConnectionInfoSecurityVO(ConnScope.STORAGE_CONN));
        dataSourceReq.setDatasourceConnection(datasourceConnectionVO);

        Long datasourceId = 1L;
        DataSourceBasicInfoVo basicInfoVo = DataSourceBasicInfoVo.newBuilder()
                .withId(datasourceId)
                .withDatasourceConfigInfo(hostPortDatasourceConfig)
                .withDatasourceType(dataSourceReq.getDatasourceType())
                .withName(dataSourceReq.getName())
                .withTags(dataSourceReq.getTags())
                .withUpdateUser("test")
                .withCreateTime(OffsetDateTime.now())
                .withUpdateTime(OffsetDateTime.now())
                .withUpdateUser("test")
                .build();

        String createUrl = url + "/datasource";
        BDDMockito.given(restTemplate.exchange(Mockito.eq(createUrl), Mockito.eq(HttpMethod.POST), any(), Mockito.eq(DataSourceBasicInfoVo.class)))
                .willReturn(new ResponseEntity<>(basicInfoVo, HttpStatus.OK));
        doReturn(datasourceConnectionVO.getDatasourceConnectionList()).when(connectionAppService).createDatasourceConnection(any(), any());
        DataSourceVO dataSourceVO = dataSourceAppService.add(dataSourceReq);
        assertThat(dataSourceVO.getId(), is(datasourceId));
        assertThat(dataSourceVO.getDatasourceType(), is(DatasourceType.POSTGRESQL.name()));
        assertThat(dataSourceVO.getDatasourceConnection().getUserConnectionList().size(), is(1));
        assertThat(dataSourceVO.getDatasourceConnection().getDataConnection(), is(notNullValue()));
        assertThat(dataSourceVO.getDatasourceConnection().getMetadataConnection(), is(notNullValue()));
        assertThat(dataSourceVO.getDatasourceConnection().getStorageConnection(), is(notNullValue()));
        assertThat(dataSourceVO.getDatasourceConnection().getStorageConnection(), is(notNullValue()));

    }

    private ConnectionInfoSecurityVO getConnectionInfoSecurityVO(ConnScope connScope) {
        ConnectionInfoSecurityVO connectionInfoSecurityVO = new ConnectionInfoSecurityVO();
        connectionInfoSecurityVO.setId(1L);
        ConnectionConfigInfo pgConnectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432, "test1", "test_pw");
        connectionInfoSecurityVO.setConnectionConfigInfo(pgConnectionConfigInfo);
        connectionInfoSecurityVO.setConnScope(connScope);
        connectionInfoSecurityVO.setDatasourceId(1L);
        connectionInfoSecurityVO.setDeleted(false);
        connectionInfoSecurityVO.setCreateUser("user");
        connectionInfoSecurityVO.setDescription("desc");
        connectionInfoSecurityVO.setUpdateUser("user");
        connectionInfoSecurityVO.setCreateTime(OffsetDateTime.now());
        connectionInfoSecurityVO.setCreateTime(OffsetDateTime.now());
        connectionInfoSecurityVO.setSecurityUserList(ImmutableList.of("user1", "user2"));
        connectionInfoSecurityVO.setSecurityInfo(new SecurityInfo(SecurityModule.CONNECTION, 1L, ConnectionRole.CONNECTION_USER, ConnectionRole.CONNECTION_USER.getUserOperation()));
        return connectionInfoSecurityVO;
    }

    private ConnectionInfoSecurityVO mockConnectionInfoSecurityVO(ConnectionInfo connectionInfo) {
        ConnectionInfoSecurityVO connectionInfoSecurityVO = AppBasicConversionService.getSharedInstance().convert(connectionInfo, ConnectionInfoSecurityVO.class);
        connectionInfoSecurityVO.setSecurityUserList(ImmutableList.of("user1", "user2"));
        connectionInfoSecurityVO.setSecurityInfo(new SecurityInfo(SecurityModule.CONNECTION, connectionInfo.getId(), ConnectionRole.CONNECTION_USER, ConnectionRole.CONNECTION_USER.getUserOperation()));
        return connectionInfoSecurityVO;
    }

    @Test
    void test_update() {
    }

    @Test
    void test_delete() {
    }

    @Test
    void test_pull_datasource() {
    }

    @Test
    void test_fetch_latest_pull_process_by_datasource_ids() {
    }
}