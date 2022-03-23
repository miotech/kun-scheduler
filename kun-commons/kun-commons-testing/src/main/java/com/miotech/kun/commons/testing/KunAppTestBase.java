package com.miotech.kun.commons.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class KunAppTestBase {
    private static Logger log = LoggerFactory.getLogger(KunAppTestBase.class);

    private List<String> userTables;

    @Autowired
    private DataSource dataSource;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

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
                if (tableName.startsWith("kun_")) {
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

    protected static PostgreSQLContainer postgresContainer;
    protected static Neo4jContainer neo4jContainer;

    static {
        // postgresql container
        postgresContainer = new PostgreSQLContainer<>("postgres:12.3");
        postgresContainer.start();

        // neo4j container
        neo4jContainer = new Neo4jContainer("neo4j:3.5.20")
                .withoutAuthentication();
        neo4jContainer.start();
    }

    // expose datasource as properties
    @DynamicPropertySource
    static void registerDatabase(DynamicPropertyRegistry registry) {
        // postgresql properties
        registry.add("spring.datasource.url", () -> postgresContainer.getJdbcUrl() + "&stringtype=unspecified");
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.datasource.postgresql.url", () -> postgresContainer.getJdbcUrl() + "&stringtype=unspecified");
        registry.add("spring.datasource.postgresql.username", postgresContainer::getUsername);
        registry.add("spring.datasource.postgresql.password", postgresContainer::getPassword);

        // neo4j properties
        registry.add("spring.datasource.neo4j.url", () ->
                String.format("jdbc:neo4j:bolt://%s:%s", neo4jContainer.getHost(), neo4jContainer.getFirstMappedPort()));
        registry.add("spring.datasource.neo4j.driver-class-name", () -> "org.neo4j.jdbc.bolt.BoltDriver");
    }
}
