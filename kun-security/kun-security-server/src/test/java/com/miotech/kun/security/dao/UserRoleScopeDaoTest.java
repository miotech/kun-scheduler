package com.miotech.kun.security.dao;

import com.miotech.kun.security.SecurityTestBase;
import com.miotech.kun.security.factory.MockUserRoleScopeFactory;
import com.miotech.kun.security.model.bo.UserRoleScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class UserRoleScopeDaoTest extends SecurityTestBase {

    @Autowired
    private UserRoleScopeDao userRoleScopeDao;

    @Test
    public void testFindByUsernameAndModule_createThenFetch() {
        UserRoleScope userRoleScope = MockUserRoleScopeFactory.create();
        userRoleScopeDao.create(userRoleScope);

        List<UserRoleScope> userRoleScopes = userRoleScopeDao.findByUsernameAndModule(userRoleScope.getUsername(), userRoleScope.getModule());
        assertThat(userRoleScopes.size(), is(1));
        assertThat(userRoleScopes.get(0), sameBeanAs(userRoleScope));
    }

    @Test
    public void testFindByUsernameAndModule_empty() {
        List<UserRoleScope> userRoleScopes = userRoleScopeDao.findByUsernameAndModule("admin", "test");
        assertThat(userRoleScopes, empty());
    }

    @Test
    public void testFindByModuleAndRolenameAndSourceSystemId_empty() {
        List<UserRoleScope> userRoleScopes = userRoleScopeDao.findByModuleAndRolenameAndSourceSystemId("test", "viewer", "id_1");
        assertThat(userRoleScopes, empty());
    }

    @Test
    public void testFindByModuleAndRolenameAndSourceSystemId() {
        UserRoleScope userRoleScope1 = MockUserRoleScopeFactory.create("test_module1");
        userRoleScopeDao.create(userRoleScope1);

        UserRoleScope userRoleScope2 = MockUserRoleScopeFactory.create("test_module2");
        userRoleScopeDao.create(userRoleScope2);

        List<UserRoleScope> userRoleScopes = userRoleScopeDao.findByModuleAndRolenameAndSourceSystemId(userRoleScope1.getModule(), userRoleScope1.getRolename(), userRoleScope1.getSourceSystemId());
        assertThat(userRoleScopes.size(), is(1));
        assertThat(userRoleScopes.get(0), sameBeanAs(userRoleScope1));

        userRoleScopes = userRoleScopeDao.findByModuleAndRolenameAndSourceSystemId(userRoleScope2.getModule(), userRoleScope2.getRolename(), userRoleScope2.getSourceSystemId());
        assertThat(userRoleScopes.size(), is(1));
        assertThat(userRoleScopes.get(0), sameBeanAs(userRoleScope2));
    }

    @Test
    public void testDelete_empty() {
        UserRoleScope userRoleScope = MockUserRoleScopeFactory.create();
        userRoleScopeDao.delete(userRoleScope);
    }

    @Test
    public void testDelete_createThenDelete() {
        UserRoleScope userRoleScope = MockUserRoleScopeFactory.create();
        userRoleScopeDao.create(userRoleScope);
        List<UserRoleScope> userRoleScopes = userRoleScopeDao.findByUsernameAndModule(userRoleScope.getUsername(), userRoleScope.getModule());
        assertThat(userRoleScopes.size(), is(1));

        userRoleScopeDao.delete(userRoleScope);
        userRoleScopes = userRoleScopeDao.findByUsernameAndModule(userRoleScope.getUsername(), userRoleScope.getModule());
        assertThat(userRoleScopes, empty());
    }

}
