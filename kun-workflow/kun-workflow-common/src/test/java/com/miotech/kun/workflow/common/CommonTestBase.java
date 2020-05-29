package com.miotech.kun.workflow.common;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.workflow.utils.PropertyUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class CommonTestBase {
    private final Logger logger = LoggerFactory.getLogger(CommonTestBase.class);
    protected Injector injector;

    @Before
    public void setUp() {
        Properties props = PropertyUtils.loadAppProps();

        injector = Guice.createInjector(
                new CommonTestModule(props),
                new CommonModule()
        );
        injector.injectMembers(this);

        // Recreate Database Schema
        dropAllTables();
        createTables();
    }

    @After
    public void tearDown() {
        dropAllTables();
    }

    public void createTables() {
        DataSource dataSource = injector.getInstance(DataSource.class);

        try(Connection conn = dataSource.getConnection()) {
            InputStream inputStream = CommonTestBase.class
                    .getClassLoader()
                    .getResourceAsStream("sqls/init.sql");
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(inputStream));
            String createDDL = "";
            String line;
            while((line = reader.readLine())!=null) {
                createDDL += line;
            }

            String createJsonbDomain = "CREATE DOMAIN IF NOT EXISTS \"JSONB\" AS TEXT;";
            conn.createStatement().execute(createJsonbDomain);
            conn.createStatement().execute(createDDL);
        } catch (SQLException | IOException e) {
            logger.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }

    public void dropAllTables() {
        DataSource dataSource = injector.getInstance(DataSource.class);
        try (Connection conn = dataSource.getConnection()) {
            List<String> tables = Lists.newArrayList();

            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT * FROM INFORMATION_SCHEMA.TABLES" +
                            " where table_schema not in ('information_schema')");
            while (rs.next()) {
                String tableSchema = rs.getString("table_schema");
                String tableName = rs.getString("table_name");

                tables.add(tableName);
            }
            for (String table: tables) {
                conn.createStatement().execute("DROP TABLE " + table + " CASCADE");
            }
        } catch (SQLException e) {
            logger.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }
}
