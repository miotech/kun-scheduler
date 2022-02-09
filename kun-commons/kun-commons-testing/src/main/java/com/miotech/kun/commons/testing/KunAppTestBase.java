package com.miotech.kun.commons.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.db.DatabaseOperator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * @program: kun
 * @description: kun app test base
 * @author: zemin  huang
 * @create: 2022-01-28 17:36
 **/
@SpringBootTest(classes = KunAppTestBase.TestConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
@Testcontainers
public class KunAppTestBase{

    private static final Logger logger = LoggerFactory.getLogger(KunAppTestBase.class);

    private static final String POSTGRES_IMAGE = "postgres:12.3";

    @Autowired
    DataSource dataSource;


    @Container
    public static PostgreSQLContainer postgres = startPostgres();

    private ImmutableList<String> userTables;

    @BeforeEach
    public void contextConfigurationLoad() {
//        Based on spring automatic configuration, the configuration information under the resources dir is read by default
    }

    @AfterEach
    public void clearDown() {
        truncateAllTables();

    }

    protected void subConfiguration() {
        // do nothing
    }


    public static PostgreSQLContainer startPostgres() {
        PostgreSQLContainer postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE);
        return postgres;
    }


    private void truncateAllTables() {
        DatabaseOperator operator = new DatabaseOperator(dataSource);
        for (String t : userTables(dataSource)) {
            operator.update(String.format("TRUNCATE TABLE %s;", t));
        }
    }

    private List<String> userTables(DataSource dataSource) {
        if (userTables != null) {
            return userTables;
        }

        try (Connection conn = dataSource.getConnection()) {
            List<String> tables = Lists.newArrayList();
            ResultSet rs = conn.getMetaData()
                    .getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString(3);
                if (tableName.startsWith("kun_")) {
                    tables.add(tableName);
                }
            }
            userTables = ImmutableList.copyOf(tables);
            return userTables;
        } catch (SQLException e) {
            logger.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }


    @Configuration
    @SpringBootApplication(scanBasePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.security",
            "com.miotech.kun.dataplatform",
            "com.miotech.kun.datadashboard",
            "com.miotech.kun.dataquality",
            "com.miotech.kun.datadiscovery",
            "com.miotech.kun.webapp",
            "com.miotech.kun.monitor",
            "com.miotech.kun.openapi"
    })
    public static class TestConfiguration {

    }


}
