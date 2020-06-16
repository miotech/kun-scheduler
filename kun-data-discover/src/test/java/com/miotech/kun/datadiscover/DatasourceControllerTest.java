package com.miotech.kun.datadiscover;

import com.google.common.net.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author: Jie Chen
 * @created: 2020/6/12
 */
@Slf4j
public class DatasourceControllerTest extends BaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    String apiPrefix = "/kun/api/v1";

    @Test
    public void testGetDatasets() {
        jdbcTemplate.query("select * from kun_mt_datasource_type", rs -> {
            do {
                log.info("datasource type: " + rs.getString("name"));
            } while (rs.next());
        });
    }
}
