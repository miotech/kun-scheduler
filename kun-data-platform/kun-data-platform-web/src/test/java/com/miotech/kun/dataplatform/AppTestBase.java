package com.miotech.kun.dataplatform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.dataplatform.web.common.tasktemplate.service.TaskTemplateLoader;
import com.miotech.kun.dataplatform.web.config.TestOnlyController;
import com.miotech.kun.dataplatform.web.config.TestWorkflowConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@SpringBootTest(classes = AppTestBase.TestConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public abstract class AppTestBase {

    private List<String> userTables;

    @Autowired
    private DataSource  dataSource;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskTemplateLoader taskTemplateLoader;

    @BeforeEach
    public void init() {
        taskTemplateLoader.persistTemplates();
    }

    @AfterEach
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

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.security",
            "com.miotech.kun.dataplatform",
            "com.miotech.kun.monitor"
    })
    @Import({TestWorkflowConfig.class, TestOnlyController.class})
    public static class TestConfiguration {

    }
}
