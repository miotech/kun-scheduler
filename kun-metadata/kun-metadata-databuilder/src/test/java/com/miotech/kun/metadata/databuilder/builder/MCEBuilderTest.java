package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.common.service.FilterRuleService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.PostgresConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.filter.FilterRule;
import com.miotech.kun.metadata.core.model.filter.FilterRuleType;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import com.miotech.kun.metadata.databuilder.container.PostgreSQLTestContainer;
import com.miotech.kun.metadata.databuilder.context.ApplicationContext;
import com.miotech.kun.metadata.databuilder.factory.DataSourceFactory;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.load.impl.PostgresLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

public class MCEBuilderTest extends DatabaseTestBase {

    private static final String TABLE_NAME = "bar";

    @Inject
    public DataSourceService dataSourceService;

    @Inject
    public MetadataDatasetService metadataDatasetService;
    @Inject
    public FilterRuleService filterRuleService;

    @Inject
    public MCEBuilder mceBuilder;
    public static final String tableName = "bar";

    public static final String databaseName = "test.public";

    @Override
    protected void configuration() {
        super.configuration();
        bind(Loader.class, PostgresLoader.class);
        bind(EventPublisher.class, NopEventPublisher.class);
    }

    @BeforeEach
    public void setUp() {
        ApplicationContext.init(new Props());
    }

    @Test
    public void testExtractSchemaOfDataSource_success() {

        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        DataSource dataSource = mockPgDatasource(postgreSQLContainer);
        // Execute
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        // Assert
        Optional<Dataset> datasetOptional = metadataDatasetService.fetchDataSet(dataSource.getId(), postgreSQLContainer.getDatabaseName(), tableName);
        Assertions.assertFalse(datasetOptional.isPresent());
        String mceRule = FilterRuleType.mceRule("%", "%", "%", "%");
        FilterRule filterRule = FilterRule.FilterRuleBuilder.builder().withType(FilterRuleType.MCE).withPositive(true).withRule(mceRule).build();
        filterRuleService.addFilterRule(filterRule);
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOptional1 = metadataDatasetService.fetchDataSet(dataSource.getId(), databaseName, tableName);
        Assertions.assertTrue(datasetOptional1.isPresent());
        Dataset dataset1 = datasetOptional1.get();
        assertThat(dataset1, is(notNullValue()));
        assertThat(dataset1.getName(), is(TABLE_NAME));
        assertThat(dataset1.isDeleted(), is(false));
        assertThat(dataset1.getFields().size(), is(2));
        List<DatasetField> fields = dataset1.getFields();

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

    private DataSource mockPgDatasource(PostgreSQLContainer postgreSQLContainer) {
        // Mock DataSource
        ConnectionConfigInfo connectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort()
                , postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());
        Map<String, Object> hostPortDatasourceConfig = DataSourceFactory.createHostPortDatasourceConfig(postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort());
        DataSourceRequest sourceRequest = DataSourceFactory.createDataSourceRequest("pg", hostPortDatasourceConfig, connectionConfigInfo, DatasourceType.POSTGRESQL);
        return dataSourceService.create(sourceRequest);
    }

    @Test
    public void testExtractSchemaOfDataset_dropTableThenPull() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");
        // Mock DataSource
        DataSource dataSource = mockPgDatasource(postgreSQLContainer);
        String mceRule = FilterRuleType.mceRule("%", "%", "%", "%");
        FilterRule filterRule = FilterRule.FilterRuleBuilder.builder().withType(FilterRuleType.MCE).withPositive(true).withRule(mceRule).build();
        filterRuleService.addFilterRule(filterRule);
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOptional = metadataDatasetService.fetchDataSet(dataSource.getId(), databaseName, tableName);
        Assertions.assertTrue(datasetOptional.isPresent());
        Dataset dataset1 = datasetOptional.get();
        // Drop table in postgresql
        dropTable(postgreSQLContainer, TABLE_NAME);
        // Execute
        mceBuilder.extractSchemaOfDataset(dataset1.getGid());
        // Validate
        Optional<Dataset> datasetOpt = metadataDatasetService.fetchDatasetByGid(dataset1.getGid());
        assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().isDeleted(), is(true));
    }

    @Test
    public void testExtractSchemaOfDataset_dropTableAndRecreateThenPull() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        DataSource dataSource = mockPgDatasource(postgreSQLContainer);
        String mceRule = FilterRuleType.mceRule("%", "%", "%", "%");

        FilterRule filterRule = FilterRule.FilterRuleBuilder.builder().withType(FilterRuleType.MCE).withPositive(true).withRule(mceRule).build();
        filterRuleService.addFilterRule(filterRule);
        // Extract schema by datasource
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOpt = metadataDatasetService.fetchDataSet(dataSource.getId(), databaseName, tableName);
        assertTrue(datasetOpt.isPresent());

        // Drop table in postgresql
        dropTable(postgreSQLContainer, TABLE_NAME);

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());
        // Validate
        datasetOpt = metadataDatasetService.fetchDatasetByGid(datasetOpt.get().getGid());
        assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().isDeleted(), is(true));

        // ReCreate table
        createTable(postgreSQLContainer, TABLE_NAME);

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());

        // Validate
        datasetOpt = metadataDatasetService.fetchDatasetByGid(datasetOpt.get().getGid());
        assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().isDeleted(), is(false));
        assertThat(datasetOpt.get().getFields().size(), is(1));
    }

    @Test
    public void testExtractSchemaOfDataset_addColumnThenPull() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        DataSource dataSource = mockPgDatasource(postgreSQLContainer);
        String mceRule = FilterRuleType.mceRule("%", "%", "%", "%");
        FilterRule filterRule = FilterRule.FilterRuleBuilder.builder().withType(FilterRuleType.MCE).withPositive(true).withRule(mceRule).build();
        filterRuleService.addFilterRule(filterRule);
        // Extract schema by datasource
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOpt = metadataDatasetService.fetchDataSet(dataSource.getId(), databaseName, tableName);
        Assertions.assertTrue(datasetOpt.isPresent());


        // Add column
        addColumn(postgreSQLContainer, TABLE_NAME, "test_column", "int4");

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());

        // Validate
        datasetOpt = metadataDatasetService.fetchDatasetByGid(datasetOpt.get().getGid());
        Assertions.assertTrue(datasetOpt.isPresent());
        assertThat(datasetOpt.get().getFields().size(), is(3));
    }

    @Test
    public void testExtractSchemaOfDataset_dropColumnThenPull() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        DataSource dataSource = mockPgDatasource(postgreSQLContainer);
        String mceRule = FilterRuleType.mceRule("%", "%", "%", "%");
        FilterRule filterRule = FilterRule.FilterRuleBuilder.builder().withType(FilterRuleType.MCE).withPositive(true).withRule(mceRule).build();
        filterRuleService.addFilterRule(filterRule);
        // Extract schema by datasource
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOpt = metadataDatasetService.fetchDataSet(dataSource.getId(), databaseName, tableName);
        assertTrue(datasetOpt.isPresent());

        // Drop column
        dropColumn(postgreSQLContainer, TABLE_NAME, "id");

        // Execute
        mceBuilder.extractSchemaOfDataset(datasetOpt.get().getGid());

        // Validate
        datasetOpt = metadataDatasetService.fetchDatasetByGid(datasetOpt.get().getGid());
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
