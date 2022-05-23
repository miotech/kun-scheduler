package com.miotech.kun.security.dao;

import com.miotech.kun.security.SecurityTestBase;
import com.miotech.kun.security.factory.MockRoleFactory;
import com.miotech.kun.security.model.bo.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class RoleDaoTest extends SecurityTestBase {

    @Autowired
    private RoleDao roleDao;

    @Test
    public void testCreate_success() {
        Role role = MockRoleFactory.create();
        roleDao.create(role);

        Role roleOfFetched = roleDao.findByModuleAndName(role.getModule(), role.getName());
        assertThat(roleOfFetched, sameBeanAs(role));
    }

    @Test
    public void testCreate_duplicateKey() {
        Role role = MockRoleFactory.create();
        roleDao.create(role);
        try {
            roleDao.create(role);
        } catch (Exception e) {
            assertThat(e, instanceOf(DuplicateKeyException.class));
        }

    }

}
