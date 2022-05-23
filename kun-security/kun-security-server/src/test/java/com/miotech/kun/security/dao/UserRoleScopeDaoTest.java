package com.miotech.kun.security.dao;

import com.miotech.kun.security.SecurityTestBase;
import com.miotech.kun.security.factory.MockUserRoleScopeFactory;
import com.miotech.kun.security.model.bo.UserRoleScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
