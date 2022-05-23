package com.miotech.kun.security.dao;

import com.miotech.kun.security.SecurityTestBase;
import com.miotech.kun.security.factory.MockUpdateScopeRequestFactory;
import com.miotech.kun.security.factory.MockUserRoleRequestFactory;
import com.miotech.kun.security.model.bo.ResourceRole;
import com.miotech.kun.security.model.bo.UpdateScopeRequest;
import com.miotech.kun.security.model.bo.UserRoleRequest;
import com.miotech.kun.security.model.bo.UserRoleWithScope;
import com.miotech.kun.security.service.UserRoleScopeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class UserRoleScopeServiceTest extends SecurityTestBase {

    @Autowired
    private UserRoleScopeService userRoleScopeService;

    /**
     * prepare data：
     * username     module          rolename    source_system_id
     * admin        test_module     viewer      id_1
     * admin        test_module     viewer      id_2
     *
     * query param:
     * username     module          source_system_ids
     * admin        test_module     id_1
     *
     * should return:
     * username     module          resource        role
     * admin        test_module     id_1            viewer
     *
     */
    @Test
    public void testFindRoleOnSpecifiedResources_case1() {
        UpdateScopeRequest updateScopeRequest = MockUpdateScopeRequestFactory.create("admin", "test_module", "viewer", ImmutableList.of("id_1", "id_2"));
        userRoleScopeService.addScopeOnSpecifiedRole(updateScopeRequest);

        UserRoleRequest userRoleRequest = MockUserRoleRequestFactory.create(updateScopeRequest.getUsername(), updateScopeRequest.getModule(), ImmutableList.of("id_1"));
        UserRoleWithScope userRoleWithScope = userRoleScopeService.findRoleOnSpecifiedResources(userRoleRequest);
        assertThat(userRoleWithScope.getUsername(), is(userRoleRequest.getUsername()));
        assertThat(userRoleWithScope.getModule(), is(userRoleRequest.getModule()));
        assertThat(userRoleWithScope.getResourceRoles().size(), is(1));
        ResourceRole resourceRole = userRoleWithScope.getResourceRoles().get(0);
        assertThat(resourceRole.getSourceSystemId(), is("id_1"));
        assertThat(resourceRole.getRolename(), is("viewer"));
    }

    /**
     * prepare data：
     * username     module          rolename    source_system_id
     * admin        test_module     viewer      id_1
     * admin        test_module     viewer      id_2
     *
     * query param:
     * username     module          source_system_ids
     * admin        test_module     id_3
     *
     * should return:
     * username     module          resource        role
     * admin        test_module     id_3            null
     *
     */
    @Test
    public void testFindRoleOnSpecifiedResources_case2() {
        UpdateScopeRequest updateScopeRequest = MockUpdateScopeRequestFactory.create("admin", "test_module", "viewer", ImmutableList.of("id_1", "id_2"));
        userRoleScopeService.addScopeOnSpecifiedRole(updateScopeRequest);

        UserRoleRequest userRoleRequest = MockUserRoleRequestFactory.create(updateScopeRequest.getUsername(), updateScopeRequest.getModule(), ImmutableList.of("id_3"));
        UserRoleWithScope userRoleWithScope = userRoleScopeService.findRoleOnSpecifiedResources(userRoleRequest);
        assertThat(userRoleWithScope.getUsername(), is(userRoleRequest.getUsername()));
        assertThat(userRoleWithScope.getModule(), is(userRoleRequest.getModule()));
        assertThat(userRoleWithScope.getResourceRoles().size(), is(1));
        ResourceRole resourceRole = userRoleWithScope.getResourceRoles().get(0);
        assertThat(resourceRole.getSourceSystemId(), is("id_3"));
        assertThat(resourceRole.getRolename(), nullValue());
    }

    /**
     * prepare data：
     * username     module          rolename    source_system_id
     * admin        test_module     viewer      id_1
     * admin        test_module     viewer      id_2
     *
     * query param:
     * username     module          source_system_ids
     * admin        test_module     id_1,id_3
     *
     * should return:
     * username     module          resource        role
     * admin        test_module     id_1            viewer
     *                              id_3            null
     *
     */
    @Test
    public void testFindRoleOnSpecifiedResources_case3() {
        UpdateScopeRequest updateScopeRequest = MockUpdateScopeRequestFactory.create("admin", "test_module", "viewer", ImmutableList.of("id_1", "id_2"));
        userRoleScopeService.addScopeOnSpecifiedRole(updateScopeRequest);

        UserRoleRequest userRoleRequest = MockUserRoleRequestFactory.create(updateScopeRequest.getUsername(), updateScopeRequest.getModule(), ImmutableList.of("id_1", "id_3"));
        UserRoleWithScope userRoleWithScope = userRoleScopeService.findRoleOnSpecifiedResources(userRoleRequest);
        assertThat(userRoleWithScope.getUsername(), is(userRoleRequest.getUsername()));
        assertThat(userRoleWithScope.getModule(), is(userRoleRequest.getModule()));
        assertThat(userRoleWithScope.getResourceRoles().size(), is(2));
        for (ResourceRole resourceRole : userRoleWithScope.getResourceRoles()) {
            if (resourceRole.getSourceSystemId().equals("id_1")) {
                assertThat(resourceRole.getRolename(), is("viewer"));
            } else if (resourceRole.getSourceSystemId().equals("id_3")) {
                assertThat(resourceRole.getRolename(), nullValue());
            } else {
                throw new IllegalArgumentException("Invalid sourceSystemId: " + resourceRole.getSourceSystemId());
            }
        }
    }

    /**
     * prepare data：
     * username     module          rolename    source_system_id
     * admin        test_module     viewer      id_1
     * admin        test_module     viewer      id_2
     *
     * query param:
     * username     module                  source_system_ids
     * admin        another_test_module     id_1
     *
     * should return:
     * username     module                  resource        role
     * admin        another_test_module     id_1            null
     *
     */
    @Test
    public void testFindRoleOnSpecifiedResources_case4() {
        UpdateScopeRequest updateScopeRequest = MockUpdateScopeRequestFactory.create("admin", "test_module", "viewer", ImmutableList.of("id_1", "id_2"));
        userRoleScopeService.addScopeOnSpecifiedRole(updateScopeRequest);

        String anotherTestModule = "another_test_module";
        UserRoleRequest userRoleRequest = MockUserRoleRequestFactory.create(updateScopeRequest.getUsername(), anotherTestModule, ImmutableList.of("id_1"));
        UserRoleWithScope userRoleWithScope = userRoleScopeService.findRoleOnSpecifiedResources(userRoleRequest);
        assertThat(userRoleWithScope.getUsername(), is(userRoleRequest.getUsername()));
        assertThat(userRoleWithScope.getModule(), is(anotherTestModule));
        assertThat(userRoleWithScope.getResourceRoles().size(), is(1));
        ResourceRole resourceRole = userRoleWithScope.getResourceRoles().get(0);
        assertThat(resourceRole.getSourceSystemId(), is("id_1"));
        assertThat(resourceRole.getRolename(), nullValue());
    }

}
