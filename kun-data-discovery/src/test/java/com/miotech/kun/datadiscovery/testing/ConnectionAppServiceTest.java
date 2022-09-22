package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.datadiscovery.model.entity.SecurityInfo;
import com.miotech.kun.datadiscovery.model.enums.ConnectionRole;
import com.miotech.kun.datadiscovery.model.enums.SecurityModule;
import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoSecurityVO;
import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoVO;
import com.miotech.kun.datadiscovery.service.ConnectionAppService;
import com.miotech.kun.datadiscovery.testing.mockdata.MockConnectionFactory;
import com.miotech.kun.datadiscovery.testing.mockdata.MockSecurityRpcClientFactory;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.vo.UniversalSearchInfo;
import com.miotech.kun.security.common.KunRole;
import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp;
import com.miotech.kun.security.facade.rpc.ScopeRole;
import junit.runner.BaseTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ConnectionAppServiceTest extends DataDiscoveryTestBase {

    @Autowired
    private ConnectionAppService connectionAppService;

    public static final SecurityModule SECURITY_MODULE = SecurityModule.CONNECTION;

    @Test
    void test_find_ById_Vo() {
        Long id = 1L;
        Long datasourceId = 1L;
        String urlFormat = url + "/connection/%d";
        String fullUr = String.format(urlFormat, id);
        ConnectionConfigInfo userConnectionConfigInfo = new AthenaConnectionConfigInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        ConnectionInfo connectionInfo = MockConnectionFactory.mockConnectionInfo(id, "user_connection", datasourceId, ConnScope.USER_CONN, userConnectionConfigInfo, "test_desc");
        BDDMockito.given(restTemplate.exchange(Mockito.eq(fullUr), Mockito.eq(HttpMethod.GET), Mockito.eq(null), Mockito.eq(ConnectionInfo.class)))
                .willReturn(new ResponseEntity<>(connectionInfo, HttpStatus.OK));
        ConnectionInfoVO basicInfo = connectionAppService.findBasicInfoById(id);
        assertThat(basicInfo.getId(), is(id));
        Long errorId = 2L;
        String fullUr2 = String.format(urlFormat, errorId);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(fullUr2), Mockito.eq(HttpMethod.GET), Mockito.eq(null), Mockito.eq(ConnectionInfo.class)))
                .willReturn(new ResponseEntity<>(null, HttpStatus.OK));
        Exception ex1 = assertThrows(IllegalArgumentException.class, () -> connectionAppService.findBasicInfoById(errorId));
        Assertions.assertEquals(String.format("connection not exist:%s", errorId), ex1.getMessage());
    }

    @Test
    void test_find_Security_Vo() {
        Long id = 1L;
        Long datasourceId = 1L;
        String urlFormat = url + "/connection/%d";
        String fullUr = String.format(urlFormat, id);
        ConnectionConfigInfo userConnectionConfigInfo = new AthenaConnectionConfigInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        ConnectionInfo connectionInfo = MockConnectionFactory.mockConnectionInfo(id, "user_connection", datasourceId, ConnScope.USER_CONN, userConnectionConfigInfo, "test_desc");
        BDDMockito.given(restTemplate.exchange(Mockito.eq(fullUr), Mockito.eq(HttpMethod.GET), Mockito.eq(null), Mockito.eq(ConnectionInfo.class)))
                .willReturn(new ResponseEntity<>(connectionInfo, HttpStatus.OK));
        ConnectionInfoSecurityVO info = connectionAppService.findSecurityConnectionInfoById(id);
        assertThat(info.getId(), is(id));
        SecurityInfo securityInfo = info.getSecurityInfo();
        assertThat(securityInfo.getSecurityModule(), is(SECURITY_MODULE));
        assertThat(securityInfo.getSourceSystemId(), is(id));
        assertThat(securityInfo.getKunRole(), is(ConnectionRole.CONNECTION_USER));
        assertThat(securityInfo.getOperations(), is(ConnectionRole.CONNECTION_USER.getUserOperation()));
        assertThat(info.getSecurityUserList(), is(Collections.emptyList()));
        ScopeRole scopeRole1 = ScopeRole.newBuilder().setSourceSystemId(String.valueOf(id)).setRolename(ConnectionRole.CONNECTION_MANAGER.getName()).build();
        ScopeRole scopeRole2 = ScopeRole.newBuilder().setSourceSystemId(String.valueOf(id)).setRolename(ConnectionRole.CONNECTION_USER.getName()).build();
        RoleOnSpecifiedResourcesResp resp = MockSecurityRpcClientFactory.mockUserRoleRespGlossary("test", SECURITY_MODULE.name(), ImmutableList.of(scopeRole1, scopeRole2));
        Mockito.when(securityRpcClient.findRoleOnSpecifiedResources(SECURITY_MODULE.name(), Collections.singletonList(String.valueOf(id)))).thenReturn(resp);
        ConnectionInfoSecurityVO securityVO = connectionAppService.findSecurityConnectionInfoById(id);
        assertThat(securityVO.getSecurityInfo().getSourceSystemId(), is(id));
        assertThat(securityVO.getSecurityInfo().getKunRole(), is(ConnectionRole.CONNECTION_MANAGER));


    }
}