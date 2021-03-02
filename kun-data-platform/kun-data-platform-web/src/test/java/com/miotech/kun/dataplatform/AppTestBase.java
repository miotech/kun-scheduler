package com.miotech.kun.dataplatform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.dataplatform.common.tasktemplate.service.TaskTemplateLoader;
import com.miotech.kun.dataplatform.config.EventNotifyConfig;
import com.miotech.kun.dataplatform.config.ZhongdaServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DataPlatformApplication.class, TestWorkflowConfig.class, TestOnlyController.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class AppTestBase {

    private List<String> userTables;

    @Autowired
    private DataSource  dataSource;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskTemplateLoader taskTemplateLoader;

    // Temporarily use mocked beans for zhongda notifier configuration
    @MockBean
    private ZhongdaServiceConfig zhongdaNotifierConfig;

    // Temporarily use mocked beans for event notifier configuration
    @MockBean
    private EventNotifyConfig eventNotifyConfig;

    @Before
    public void init() {
        taskTemplateLoader.persistTemplates();
    }

    @After
    public void tearDown() {
        truncateAllTables();
    }

    private void truncateAllTables() {
        for (String t : inferUserTables(dataSource)) {
            jdbcTemplate.update(String.format("TRUNCATE TABLE %s;", t));
        }
    }

    private List<String> inferUserTables(DataSource dataSource) {
        if (userTables != null) {
            return userTables;
        }

        try (Connection conn = dataSource.getConnection()) {
            List<String> tables = Lists.newArrayList();
            ResultSet rs = conn.getMetaData()
                    .getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString(3);
                if (tableName.startsWith("kun_dp")) {
                    tables.add(tableName);
                }
            }
            userTables = ImmutableList.copyOf(tables);
            return userTables;
        } catch (SQLException e) {
            log.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }

    @Test
    public void defaultTest(){
        // DO NOT REMOVE THIS TEST CASE
        // AppTestBase requires at least one test case to bootstrap
    }
}
