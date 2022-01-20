package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFieldFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFieldStatFactory;
import com.miotech.kun.metadata.core.model.connection.AthenaConnectionInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.HiveMetaStoreConnectionInfo;
import com.miotech.kun.metadata.core.model.dataset.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.*;
import com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class MetadataDatasetDaoTest extends DatabaseTestBase {
    @Inject
    private MetadataDatasetDao metadataDatasetDao;

    @Inject
    private DatabaseOperator databaseOperator;

    @Inject
    private DataSourceDao dataSourceDao;

    @Inject
    private TagDao tagDao;

    @Inject
    private javax.sql.DataSource dataSource;

    @Override
    protected boolean usePostgres() {
        return true;
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
        ((HikariDataSource) dataSource).close();
    }

    @Test
    public void fetchDatasetByGid_withExistGid_shouldReturnResultProperly() {
        // 1. Prepare
        // Force insert a dataset record into database
        databaseOperator.update(
                "INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES (?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                1L,
                "example_dataset",
                3L,
                "{\"type\": \"ARANGO_COLLECTION\", \"@class\": \"com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore\", \"database\": \"miotech_test_database\", \"collection\": \"demo_collection\", \"host\": \"127.0.0.1\", \"port\": 7890}",
                "miotech_test_database",
                "arango:collection=demo_collection,database=miotech_test_database,host=127.0.0.1,port=7890",
                true
        );

        databaseOperator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type, description, raw_type) VALUES (?, ?, ?, ?, ?)",
                1L,
                "id",
                "NUMBER",
                "",
                "decimal(18,0)");

        DataStore dataStore = new ArangoCollectionStore(
                "127.0.0.1",
                7890,
                "miotech_test_database",
                "demo_collection"
        );

        // 2. Execute
        Optional<Dataset> datasetOptional = metadataDatasetDao.fetchDatasetByGid(1L);

        // 3. Validate
        assertTrue(datasetOptional.isPresent());
        Dataset dataset = datasetOptional.get();
        assertThat(dataset.getGid(), is(1L));
        assertThat(dataset.getName(), is("example_dataset"));
        assertThat(dataset.getDatabaseName(), is("miotech_test_database"));
        assertThat(dataset.getDataStore(), sameBeanAs(dataStore));
        assertThat(dataset.isDeleted(), is(true));
        assertThat(dataset.getFields().size(), is(1));
    }

    @Test
    public void fetchDatasetByGid_withNonExistGid_shouldReturnEmptyOptionalObject() {
        // Execute
        Optional<Dataset> datasetOptional = metadataDatasetDao.fetchDatasetByGid(1L);

        // Validate
        assertFalse(datasetOptional.isPresent());
    }

    @Test
    public void testFindByName_withNonExistName() {
        // Execute
        Optional<Dataset> datasetOpt = metadataDatasetDao.findByName("NonExistName");

        // Validate
        assertFalse(datasetOpt.isPresent());
    }

    @Test
    public void testFindByName_withExistName() {
        String name = "example_dataset";
        String fieldName = "id";
        // 1. Prepare
        // Force insert a dataset record into database
        databaseOperator.update(
                "INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES (?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                1L,
                name,
                3L,
                "{\"type\": \"ARANGO_COLLECTION\", \"@class\": \"com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore\", \"database\": \"miotech_test_database\", \"collection\": \"demo_collection\", \"host\": \"127.0.0.1\", \"port\": 7890}",
                "miotech_test_database",
                "arango:collection=demo_collection,database=miotech_test_database,host=127.0.0.1,port=7890",
                true
        );

        databaseOperator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type, description, raw_type) VALUES (?, ?, ?, ?, ?)",
                1L,
                fieldName,
                "NUMBER",
                "",
                "decimal(18,0)");

        // Execute
        Optional<Dataset> datasetOpt = metadataDatasetDao.findByName(name);

        // Validate
        assertTrue(datasetOpt.isPresent());
        Dataset dataset = datasetOpt.get();
        assertThat(dataset.getName(), is(name));
        assertThat(dataset.getFields().size(), is(1));
        DatasetField datasetField = dataset.getFields().get(0);
        assertThat(datasetField.getName(), is(fieldName));
    }

    @Test
    public void testGetDatabases_emptyParam() {
        List<Long> emptyParams = ImmutableList.of();
        List<DatabaseBaseInfo> databases = metadataDatasetDao.getDatabases(emptyParams);
        assertThat(databases, empty());
    }

    @Test
    public void testGetDatabases() {
        Long gid = IdGenerator.getInstance().nextId();
        Dataset dataset = MockDatasetFactory.createDataset(gid);
        metadataDatasetDao.createDataset(dataset);

        List<Long> dataSourceIds = ImmutableList.of(dataset.getDatasourceId());
        List<DatabaseBaseInfo> databases = metadataDatasetDao.getDatabases(dataSourceIds);
        assertThat(databases.size(), is(1));
    }

    @Test
    public void testSearchDatasets_emptyKeyword() {
        BasicSearchRequest searchRequest = new BasicSearchRequest();
        DatasetBasicSearch datasetBasicSearch = metadataDatasetDao.searchDatasets(searchRequest);

        assertThat(datasetBasicSearch.getDatasets(), empty());
    }

    @Test
    public void testSearchDatasets() {
        Long gid1 = IdGenerator.getInstance().nextId();
        Long gid2 = IdGenerator.getInstance().nextId();
        Dataset dataset1 = MockDatasetFactory.createDataset(gid1, "test1");
        Dataset dataset2 = MockDatasetFactory.createDataset(gid2, "test2");
        metadataDatasetDao.createDataset(dataset1);
        metadataDatasetDao.createDataset(dataset2);

        DataSource dataSource = MockDataSourceFactory.createDataSource(dataset1.getDatasourceId(), "test datasource", ConnectionConfig.newBuilder()
                .withUserConnection(new HiveMetaStoreConnectionInfo(ConnectionType.HIVE_THRIFT, "thrift://127.0.0.1:10000"))
                .build(), DatasourceType.HIVE, ImmutableList.of());
        dataSourceDao.create(dataSource);

        BasicSearchRequest searchRequest = new BasicSearchRequest("test");
        DatasetBasicSearch datasetBasicSearch = metadataDatasetDao.searchDatasets(searchRequest);

        assertThat(datasetBasicSearch.getDatasets().size(), is(2));
    }

    @Test
    public void testFullTextSearch() {
        Long gid1 = IdGenerator.getInstance().nextId();
        List<String> owners1 = ImmutableList.of("u1", "u2");
        List<String> tags1 = ImmutableList.of("t1", "t2");
        Long gid2 = IdGenerator.getInstance().nextId();
        List<String> owners2 = ImmutableList.of("u2", "u3");
        List<String> tags2 = ImmutableList.of("t2", "t3");
        Dataset dataset1 = MockDatasetFactory.createDataset(gid1, "test1");
        Dataset dataset2 = MockDatasetFactory.createDataset(gid2, "test2");
        Dataset dataset1OfFetch = metadataDatasetDao.createDataset(dataset1);
        Dataset dataset2OfFetch = metadataDatasetDao.createDataset(dataset2);
        OffsetDateTime now = DateTimeUtils.now();
        databaseOperator.update("INSERT INTO kun_mt_dataset_stats(dataset_gid, row_count, stats_date, last_updated_time, total_byte_size) VALUES (?, ?, ?, ?, ?)",
                dataset1OfFetch.getGid(), 1L, now, now, 1L);


        DataSource dataSource = MockDataSourceFactory.createDataSource(dataset1.getDatasourceId(), "test datasource", ConnectionConfig.newBuilder()
                .withUserConnection(new AthenaConnectionInfo(ConnectionType.ATHENA, "jdbc:awsathena://...", "username", "password"))
                .build(), DatasourceType.HIVE, ImmutableList.of());
        dataSourceDao.create(dataSource);

        metadataDatasetDao.overwriteOwners(dataset1OfFetch.getGid(), owners1);
        metadataDatasetDao.overwriteOwners(dataset2OfFetch.getGid(), owners2);

        tagDao.overwriteDatasetTags(dataset1OfFetch.getGid(), tags1);
        tagDao.overwriteDatasetTags(dataset2OfFetch.getGid(), tags2);

        DatasetSearchRequest searchRequest = new DatasetSearchRequest(dataset1.getName(), ImmutableList.of("u2"), ImmutableList.of("t2"),
                ImmutableList.of(1L), ImmutableList.of(dataset1.getDatasourceId()), ImmutableList.of(dataset1.getDatabaseName()), now.minusDays(1).toEpochSecond() * 1000, now.plusDays(1).toEpochSecond() * 1000, null, null, null);

        DatasetBasicSearch datasetBasicSearch = metadataDatasetDao.fullTextSearch(searchRequest);
        assertThat(datasetBasicSearch.getDatasets().size(), is(1));
    }

    @Test
    public void testGetDatasetDetail() {
        Long gid1 = IdGenerator.getInstance().nextId();
        List<String> owners1 = ImmutableList.of("u1", "u2");
        List<String> tags1 = ImmutableList.of("t1", "t2");
        Long gid2 = IdGenerator.getInstance().nextId();
        List<String> owners2 = ImmutableList.of("u2", "u3");
        List<String> tags2 = ImmutableList.of("t2", "t3");
        Dataset dataset1 = MockDatasetFactory.createDataset(gid1, "test1");
        Dataset dataset2 = MockDatasetFactory.createDataset(gid2, "test2");
        Dataset dataset1OfFetch = metadataDatasetDao.createDataset(dataset1);
        Dataset dataset2OfFetch = metadataDatasetDao.createDataset(dataset2);

        DataSource dataSource = MockDataSourceFactory.createDataSource(dataset1.getDatasourceId(), "test datasource", ConnectionConfig.newBuilder()
                .withUserConnection(new AthenaConnectionInfo(ConnectionType.ATHENA, "jdbc:awsathena://...", "username", "password"))
                .build(), DatasourceType.HIVE, ImmutableList.of());
        dataSourceDao.create(dataSource);

        metadataDatasetDao.overwriteOwners(dataset1OfFetch.getGid(), owners1);
        metadataDatasetDao.overwriteOwners(dataset2OfFetch.getGid(), owners2);

        tagDao.overwriteDatasetTags(dataset1OfFetch.getGid(), tags1);
        tagDao.overwriteDatasetTags(dataset2OfFetch.getGid(), tags2);

        DatasetDetail datasetDetail = metadataDatasetDao.getDatasetDetail(dataset1OfFetch.getGid());
        assertThat(datasetDetail, notNullValue());
        assertThat(datasetDetail.getGid(), is(dataset1OfFetch.getGid()));
        assertThat(datasetDetail.getName(), is(dataset1OfFetch.getName()));
        assertThat(datasetDetail.getDatasource(), is(dataSource.getName()));
        assertThat(datasetDetail.getDatabase(), is(dataset1OfFetch.getDatabaseName()));
    }

    @Test
    public void testUpdateDataset() {
        Long gid1 = IdGenerator.getInstance().nextId();
        List<String> owners1 = ImmutableList.of("u1", "u2");
        List<String> tags1 = ImmutableList.of("t1", "t2");
        Dataset dataset1 = MockDatasetFactory.createDataset(gid1, "test1");
        Dataset dataset1OfFetch = metadataDatasetDao.createDataset(dataset1);

        DataSource dataSource = MockDataSourceFactory.createDataSource(dataset1.getDatasourceId(), "test datasource", ConnectionConfig.newBuilder()
                .withUserConnection(new AthenaConnectionInfo(ConnectionType.ATHENA, "jdbc:awsathena://...", "username", "password"))
                .build(), DatasourceType.HIVE, ImmutableList.of());
        dataSourceDao.create(dataSource);
        metadataDatasetDao.overwriteOwners(dataset1OfFetch.getGid(), owners1);
        tagDao.overwriteDatasetTags(dataset1OfFetch.getGid(), tags1);

        String description = "test desc";
        List<String> owners = ImmutableList.of("u3");
        List<String> tags = ImmutableList.of("t3");
        DatasetUpdateRequest updateRequest = new DatasetUpdateRequest(description, owners, tags);
        metadataDatasetDao.updateDataset(dataset1OfFetch.getGid(), updateRequest);

        DatasetDetail datasetDetail = metadataDatasetDao.getDatasetDetail(dataset1OfFetch.getGid());
        assertThat(datasetDetail.getDescription(), is(description));
        assertThat(datasetDetail.getOwners(), containsInAnyOrder(owners.toArray()));
        assertThat(datasetDetail.getTags(), containsInAnyOrder(tags.toArray()));
    }

    @Test
    public void testSearchDatasetFields() {
        Long gid = IdGenerator.getInstance().nextId();
        Long id = IdGenerator.getInstance().nextId();
        DatasetField datasetField = MockDatasetFieldFactory.create();
        databaseOperator.update("INSERT INTO kun_mt_dataset_field(id, dataset_gid, name, type, description, raw_type) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                gid,
                datasetField.getName(),
                datasetField.getFieldType().getType().name(),
                "",
                datasetField.getFieldType().getRawType());

        FieldStatistics fieldStatistics = MockDatasetFieldStatFactory.create();
        databaseOperator.update("INSERT INTO kun_mt_dataset_field_stats(field_id, distinct_count, nonnull_count, stats_date) VALUES(?, ?, ?, ?)",
                id, fieldStatistics.getDistinctCount(), fieldStatistics.getNonnullCount(), fieldStatistics.getStatDate());

        DatasetColumnSearchRequest searchRequest = new DatasetColumnSearchRequest(datasetField.getName());
        DatasetFieldPageInfo datasetFieldPageInfo = metadataDatasetDao.searchDatasetFields(gid, searchRequest);
        assertThat(datasetFieldPageInfo.getColumns().size(), is(1));
        DatasetFieldInfo datasetFieldInfo = datasetFieldPageInfo.getColumns().get(0);
        assertThat(datasetFieldInfo.getId(), is(id));
        assertThat(datasetFieldInfo.getName(), is(datasetField.getName()));
        assertThat(datasetFieldInfo.getType(), is(datasetField.getFieldType().getType().name()));
        assertThat(datasetFieldInfo.getNotNullCount(), is(fieldStatistics.getNonnullCount()));
        assertThat(datasetFieldInfo.getDistinctCount(), is(fieldStatistics.getDistinctCount()));
    }

}