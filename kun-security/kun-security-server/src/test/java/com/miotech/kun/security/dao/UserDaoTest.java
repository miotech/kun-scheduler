package com.miotech.kun.security.dao;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.SecurityTestBase;
import com.miotech.kun.security.common.UserStatus;
import com.miotech.kun.security.factory.MockUserRequestFactory;
import com.miotech.kun.security.model.bo.UserRequest;
import com.miotech.kun.security.model.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UserDaoTest extends SecurityTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDao userDao;

    @BeforeEach
    public void clear() {
        jdbcTemplate.update("truncate table kun_security_user");
    }

    @Test
    public void testFindAll_empty() {
        List<User> users = userDao.findAll();
        assertThat(users, empty());
    }

    @Test
    public void testFindAll_createThenFind() {
        UserRequest userRequest = MockUserRequestFactory.create();
        userDao.create(userRequest);

        List<User> users = userDao.findAll();
        assertThat(users.size(), is(1));
        User user = users.get(0);
        assertThat(user.getName(), is(userRequest.getUsername()));
        assertThat(user.getEmail(), is(userRequest.getEmail()));
        assertThat(user.getWeComId(), is(userRequest.getWeComId()));
    }

    @Test
    public void testCreate_shouldSuccess() {
        UserRequest userRequest = MockUserRequestFactory.create();
        User user = userDao.create(userRequest);
        assertThat(user.getName(), is(userRequest.getUsername()));
        assertThat(user.getEmail(), is(userRequest.getEmail()));
        assertThat(user.getWeComId(), is(userRequest.getWeComId()));
    }

    @Test
    public void testCreate_usernameConflict() {
        UserRequest userRequest1 = MockUserRequestFactory.create();
        userDao.create(userRequest1);

        UserRequest userRequest2 = MockUserRequestFactory.create();
        try {
            userDao.create(userRequest2);
        } catch (Exception e) {
            assertThat(e, instanceOf(DuplicateKeyException.class));
        }
    }

    @Test
    public void testFindById_empty() {
        Long id = IdGenerator.getInstance().nextId();
        User userFindById = userDao.findById(id);
        assertThat(userFindById, nullValue());
    }

    @Test
    public void testFindById_createThenFind() {
        UserRequest userRequest = MockUserRequestFactory.create();
        User user = userDao.create(userRequest);

        User userFindById = userDao.findById(user.getId());
        assertThat(userFindById.getName(), is(userRequest.getUsername()));
        assertThat(userFindById.getEmail(), is(userRequest.getEmail()));
        assertThat(userFindById.getWeComId(), is(userRequest.getWeComId()));
    }

    @Test
    public void testFindByUsername_empty() {
        String username = StringUtils.EMPTY;
        User userFindById = userDao.findByUsername(username);
        assertThat(userFindById, nullValue());
    }

    @Test
    public void testFindByUsername_createThenFind() {
        UserRequest userRequest = MockUserRequestFactory.create();
        User user = userDao.create(userRequest);

        User userFindByUsername = userDao.findByUsername(user.getName());
        assertThat(userFindByUsername.getName(), is(userRequest.getUsername()));
        assertThat(userFindByUsername.getEmail(), is(userRequest.getEmail()));
        assertThat(userFindByUsername.getWeComId(), is(userRequest.getWeComId()));
    }

    @Test
    public void testUpdateStatus() {
        UserRequest userRequest = MockUserRequestFactory.create();
        User user = userDao.create(userRequest);

        userDao.updateStatus(user.getId(), UserStatus.DISABLE);
    }

    @Test
    public void testUpdateFullName() {
        UserRequest userRequest = MockUserRequestFactory.create();
        User user = userDao.create(userRequest);

        assertThat(user.getFullName(), is(nullValue()));

        userDao.updateFullName(user.getName(), "administrator");

        User userFindByUsername = userDao.findByUsername(user.getName());

        assertThat(userFindByUsername.getFullName(), is("administrator"));
    }
}
