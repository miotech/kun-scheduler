package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.datasource.ConnectionInfo;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.databuilder.container.PostgreSQLTestContainer;
import com.miotech.kun.metadata.databuilder.context.ApplicationContext;
import com.miotech.kun.metadata.databuilder.factory.DataSourceFactory;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.load.impl.PostgresLoader;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class MCEBuilderTest extends DatabaseTestBase {

    private static final String TABLE_NAME = "bar";

    @Inject
    public DataSourceDao dataSourceDao;

    @Inject
    public MetadataDatasetDao datasetDao;

    @Inject
    public MCEBuilder mceBuilder;

    @Override
    protected void configuration() {
        super.configuration();
        bind(Loader.class, PostgresLoader.class);
    }

    @Before
    public void setUp() {
        Props props = createProps();
        ApplicationContext.init(props);
    }

    private Props createProps() {
        Props props = new Props();
        props.put("datasource.jdbcUrl", "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
        props.put("datasource.username", "sa");
        props.put("datasource.driverClassName", "org.h2.Driver");
        return props;
    }

    @Test
    public void testExtractSchemaOfDataSource_success() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        DataSource dataSource = DataSourceFactory.create(3L,
                new ConnectionInfo(ImmutableMap.of("host", postgreSQLContainer.getHost(), "port", postgreSQLContainer.getFirstMappedPort(),
                        "username", postgreSQLContainer.getUsername(), "password", postgreSQLContainer.getPassword())));
        dataSourceDao.create(dataSource);

        // Execute
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());

        // Assert
        Optional<Dataset> datasetOpt = datasetDao.findByName(TABLE_NAME);
        assertTrue(datasetOpt.isPresent());

        Dataset dataset = datasetOpt.get();
        assertThat(dataset.getName(), is(TABLE_NAME));
        assertThat(dataset.isDeleted(), is(false));
        assertThat(dataset.getFields().size(), is(2));
        List<DatasetField> fields = dataset.getFields();

        DatasetField datasetField1 = fields.get(0);
        assertThat(datasetField1, sameBeanAs(DatasetField.newBuilder()
                .withName("id")
                .withComment(null)
                .withIsPrimaryKey(false)
                .withIsNullable(true)
                .build()).ignoring("fieldType"));
        assertThat(datasetField1.getFieldType(), sameBeanAs(new DatasetFieldType(DatasetFieldType.Type.NUMBER, "int4")));

        DatasetField datasetField2 = fields.get(1);
        assertThat(datasetField2, sameBeanAs(DatasetField.newBuilder()
                .withName("foo")
                .withComment(null)
                .withIsPrimaryKey(false)
                .withIsNullable(true)
                .build()).ignoring("fieldType"));
        assertThat(datasetField2.getFieldType(), sameBeanAs(new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "varchar")));
    }

    @Test
    public void testExtractSchemaOfDataset_dropTableThenPull() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        DataSource dataSource = DataSourceFactory.create(3L,
                new ConnectionInfo(ImmutableMap.of("host", postgreSQLContainer.getHost(), "port", postgreSQLContainer.getFirstMappedPort(),
                        "username", postgreSQLContainer.getUsername(), "password", postgreSQLContainer.getPassword())));
        dataSourceDao.create(dataSource);

        // Extract schema by datasource
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOpt = datasetDao.findByName(TABLE_NAME);
        assertTrue(datasetOpt.isPresent());

        // Drop table in postgresql
        dropTable(postgreSQLContainer, TABLE_NAME);

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());

        // Validate
        datasetOpt = datasetDao.fetchDatasetByGid(datasetOpt.get().getGid());
        assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().isDeleted(), is(true));
    }

    @Test
    public void testExtractSchemaOfDataset_dropTableAndRecreateThenPull() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        DataSource dataSource = DataSourceFactory.create(3L,
                new ConnectionInfo(ImmutableMap.of("host", postgreSQLContainer.getHost(), "port", postgreSQLContainer.getFirstMappedPort(),
                        "username", postgreSQLContainer.getUsername(), "password", postgreSQLContainer.getPassword())));
        dataSourceDao.create(dataSource);

        // Extract schema by datasource
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOpt = datasetDao.findByName(TABLE_NAME);
        assertTrue(datasetOpt.isPresent());

        // Drop table in postgresql
        dropTable(postgreSQLContainer, TABLE_NAME);

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());
        // Validate
        datasetOpt = datasetDao.fetchDatasetByGid(datasetOpt.get().getGid());
        assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().isDeleted(), is(true));

        // ReCreate table
        createTable(postgreSQLContainer, TABLE_NAME);

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());

        // Validate
        datasetOpt = datasetDao.fetchDatasetByGid(datasetOpt.get().getGid());
        assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().isDeleted(), is(false));
        assertThat(datasetOpt.get().getFields().size(), is(1));
    }

    @Test
    public void testExtractSchemaOfDataset_addColumnThenPull() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        DataSource dataSource = DataSourceFactory.create(3L,
                new ConnectionInfo(ImmutableMap.of("host", postgreSQLContainer.getHost(), "port", postgreSQLContainer.getFirstMappedPort(),
                        "username", postgreSQLContainer.getUsername(), "password", postgreSQLContainer.getPassword())));
        dataSourceDao.create(dataSource);

        // Extract schema by datasource
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOpt = datasetDao.findByName(TABLE_NAME);
        assertTrue(datasetOpt.isPresent());

        // Add column
        addColumn(postgreSQLContainer, TABLE_NAME, "test_column", "int4");

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());

        // Validate
        datasetOpt = datasetDao.fetchDatasetByGid(datasetOpt.get().getGid());
        assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().getFields().size(), is(3));
    }

    @Test
    public void testExtractSchemaOfDataset_dropColumnThenPull() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        DataSource dataSource = DataSourceFactory.create(3L,
                new ConnectionInfo(ImmutableMap.of("host", postgreSQLContainer.getHost(), "port", postgreSQLContainer.getFirstMappedPort(),
                        "username", postgreSQLContainer.getUsername(), "password", postgreSQLContainer.getPassword())));
        dataSourceDao.create(dataSource);

        // Extract schema by datasource
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOpt = datasetDao.findByName(TABLE_NAME);
        assertTrue(datasetOpt.isPresent());

        // Drop column
        dropColumn(postgreSQLContainer, TABLE_NAME, "id");

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());

        // Validate
        datasetOpt = datasetDao.fetchDatasetByGid(datasetOpt.get().getGid());
        assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().getFields().size(), is(1));
    }

    private void dropColumn(PostgreSQLContainer postgreSQLContainer, String tableName, String columnName) {
        String sql = String.format("ALTER TABLE %S DROP COLUMN %S", tableName, columnName);
        executeSQL(postgreSQLContainer, sql);
    }

    private void addColumn(PostgreSQLContainer postgreSQLContainer, String tableName, String columnName, String columnType) {
        String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnType);
        executeSQL(postgreSQLContainer, sql);
    }

    private void dropTable(PostgreSQLContainer postgreSQLContainer, String tableName) {
        String sql = "DROP TABLE " + tableName;
        executeSQL(postgreSQLContainer, sql);
    }

    private void createTable(PostgreSQLContainer postgreSQLContainer, String tableName) {
        String sql = String.format("CREATE TABLE bar(id int)");
        executeSQL(postgreSQLContainer, sql);
    }

    private void executeSQL(PostgreSQLContainer postgreSQLContainer, String sql) {
        Connection conn = null;
        Statement stat = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(String.format("jdbc:postgresql://%s:%s/test?currentSchema=%s", postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort(), "public"),
                    postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());
            stat = conn.createStatement();
            stat.execute(sql);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // do nothing
                }
            }

            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    // do nothing
                }
            }
        }
    }

}
