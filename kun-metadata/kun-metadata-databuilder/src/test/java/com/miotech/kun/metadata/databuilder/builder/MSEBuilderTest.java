package com.miotech.kun.metadata.databuilder.builder;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.common.dao.DatasetSnapshotDao;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.common.service.FilterRuleService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.PostgresConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.constant.StatisticsMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetSnapshot;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

public class MSEBuilderTest extends DatabaseTestBase {

    private static final String TABLE_NAME = "bar";

    @Inject
    private DatabaseOperator databaseOperator;
    @Inject
    public DataSourceService dataSourceService;

    @Inject
    public MetadataDatasetService metadataDatasetService;

    @Inject
    public FilterRuleService filterRuleService;

    @Inject
    private DatasetSnapshotDao datasetSnapshotDao;

    @Inject
    public MCEBuilder mceBuilder;

    @Inject
    private MSEBuilder mseBuilder;

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
    public void testExtractStatistics_success() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        ConnectionConfigInfo connectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort()
                , postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());
        Map<String, Object> hostPortDatasourceConfig = DataSourceFactory.createHostPortDatasourceConfig(postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort());
        DataSourceRequest sourceRequest = DataSourceFactory.createDataSourceRequest("pg", hostPortDatasourceConfig, connectionConfigInfo, DatasourceType.POSTGRESQL);

        DataSource dataSource = dataSourceService.create(sourceRequest);

        // Extract schema
        String mceRule = FilterRuleType.mceRule("%", "%", "%", "%");
        FilterRule filterRule = FilterRule.FilterRuleBuilder.builder().withType(FilterRuleType.MCE).withPositive(true).withRule(mceRule).build();
        filterRuleService.addFilterRule(filterRule);
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOptional1 = metadataDatasetService.fetchDataSet(dataSource.getId(), databaseName, tableName);
        assertTrue(datasetOptional1.isPresent());

        // Extract statistics
        List<DatasetSnapshot> datasetSnapshots = datasetSnapshotDao.findByDatasetGid(datasetOptional1.get().getGid());
        assertThat(datasetSnapshots.size(), is(1));
        mseBuilder.extractStatistics(datasetOptional1.get().getGid(), datasetSnapshots.get(0).getId(), StatisticsMode.FIELD);

        // Assert
        datasetSnapshots = datasetSnapshotDao.findByDatasetGid(datasetOptional1.get().getGid());
        assertThat(datasetSnapshots.size(), is(1));
        DatasetSnapshot datasetSnapshot = datasetSnapshots.get(0);
        assertThat(datasetSnapshot.getStatisticsSnapshot().getTableStatistics().getRowCount(), is(3L));
        assertThat(datasetSnapshot.getStatisticsSnapshot().getTableStatistics().getTotalByteSize(), is(8192L));
        assertThat(datasetSnapshot.getStatisticsSnapshot().getFieldStatistics().size(), is(2));
    }

    @Test
    public void testExtractStatistics_snapshotNotExist() {
        // Start PostgreSQL Container
        PostgreSQLContainer postgreSQLContainer = PostgreSQLTestContainer.executeInitSQLThenStart("sql/init_postgresql.sql");

        // Mock DataSource
        ConnectionConfigInfo connectionConfigInfo = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort()
                , postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());
        Map<String, Object> hostPortDatasourceConfig = DataSourceFactory.createHostPortDatasourceConfig(postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort());
        DataSourceRequest sourceRequest = DataSourceFactory.createDataSourceRequest("pg", hostPortDatasourceConfig, connectionConfigInfo, DatasourceType.POSTGRESQL);

        DataSource dataSource = dataSourceService.create(sourceRequest);

        // Extract schema
        String mceRule = FilterRuleType.mceRule("%", "%", "%", "%");
        FilterRule filterRule = FilterRule.FilterRuleBuilder.builder().withType(FilterRuleType.MCE).withPositive(true).withRule(mceRule).build();
        filterRuleService.addFilterRule(filterRule);
        mceBuilder.extractSchemaOfDataSource(dataSource.getId());
        Optional<Dataset> datasetOptional1 = metadataDatasetService.fetchDataSet(dataSource.getId(), databaseName, tableName);
        assertTrue(datasetOptional1.isPresent());

        // Extract statistics
        List<DatasetSnapshot> datasetSnapshots = datasetSnapshotDao.findByDatasetGid(datasetOptional1.get().getGid());
        assertThat(datasetSnapshots.size(), is(1));
        Long notExistSnapshotId = -1L;
        mseBuilder.extractStatistics(datasetOptional1.get().getGid(), notExistSnapshotId, StatisticsMode.FIELD);

        // Assert
        datasetSnapshots = datasetSnapshotDao.findByDatasetGid(datasetOptional1.get().getGid());
        assertThat(datasetSnapshots.size(), is(1));
        DatasetSnapshot datasetSnapshot = datasetSnapshots.get(0);
        assertThat(datasetSnapshot.getStatisticsSnapshot(), nullValue());
    }
}
