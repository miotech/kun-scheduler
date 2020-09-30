package com.miotech.kun.datadiscovery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: Jie Chen
 * @created: 2020/6/12
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DataDiscoveryServer.class, BaseConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BaseControllerTest {

    public static final String DEFAULT_USER = "anonymousUser";
    public static final Long DEFAULT_TIME = System.currentTimeMillis();

    String apiPrefix = "/kun/api/v1";
    protected String getUrl(String api) {
        return apiPrefix + api;
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void testDBConnection() {
        String sql = "select tag from kun_mt_tag limit 1";

        jdbcTemplate.query(sql, rs -> {
            rs.getString("tag");
        });
    }
}
