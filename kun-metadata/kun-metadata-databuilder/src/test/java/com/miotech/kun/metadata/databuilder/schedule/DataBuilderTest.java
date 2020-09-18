package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.TestContainerBuilder;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.OperatorKey;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DataBuilderTest extends DatabaseTestBase {

    private long gid = IdGenerator.getInstance().nextId();
    private String dataStoreJson = "{\"@class\":\"com.miotech.kun.workflow.core.model.lineage.MongoDataStore\",\"url\":\"test-url\",\"database\":\"test-database\",\"collection\":\"test-collection\",\"type\":\"MONGO_COLLECTION\"}";

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private TestContainerBuilder containerBuilder;

    @Test
    public void testBuildAll_openStat() {
        DataBuilder dataBuilder = buildDataBuild(true);
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerBuilder.initMongo()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildAll();

            // verify mark
            verifyLatestStates(dataBuilder.getLatestStates(), gid, dataStoreJson);
            dataBuilder.sweep();

            // verify sweep
            containerBuilder.verifyDatasetRowCount(10L);
            containerBuilder.verifyDatasetStatsRowCount(10L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildAll_notOpenStat() {
        DataBuilder dataBuilder = buildDataBuild(false);
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerBuilder.initMongo()) {
            // init data
            initDataset(gid);
            // execute biz logic
            dataBuilder.buildAll();

            // verify mark
            verifyLatestStates(dataBuilder.getLatestStates(), gid, dataStoreJson);
            dataBuilder.sweep();

            // verify sweep
            containerBuilder.verifyDatasetRowCount(10L);
            containerBuilder.verifyDatasetStatsRowCount(0L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildDatasource_mongo_openStat() {
        DataBuilder dataBuilder = buildDataBuild(true);
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerBuilder.initMongo()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);
            verifyLatestStates(dataBuilder.getLatestStates(), gid, dataStoreJson);
            dataBuilder.sweep();

            // verify
            containerBuilder.verifyDatasetRowCount(10L);
            containerBuilder.verifyDatasetStatsRowCount(10L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }


    @Test
    public void testBuildDatasource_mongo_notOpenStat() {
        DataBuilder dataBuilder = buildDataBuild(false);
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerBuilder.initMongo()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, dataStoreJson);
            dataBuilder.sweep();
            containerBuilder.verifyDatasetRowCount(10L);
            containerBuilder.verifyDatasetStatsRowCount(0L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildDatasource_postgres() {
        DataBuilder dataBuilder = buildDataBuild(false);
        // start postgres container
        try (PostgreSQLContainer postgres = containerBuilder.initPostgres()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, dataStoreJson);
            dataBuilder.sweep();
            containerBuilder.verifyDatasetRowCount(1L);
            containerBuilder.verifyDatasetStatsRowCount(0L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void testBuildDatasource_es() {
        DataBuilder dataBuilder = buildDataBuild(false);
        // start es container
        try (ElasticsearchContainer elasticsearch = containerBuilder.initEs();) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, dataStoreJson);
            dataBuilder.sweep();
            containerBuilder.verifyDatasetRowCount(1L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    @Ignore
    public void testBuildDatasource_arango() {
        DataBuilder dataBuilder = buildDataBuild(false);
        // start arango container
        try (ArangoContainer arangoContainer = containerBuilder.initArango()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, dataStoreJson);
            dataBuilder.sweep();
            containerBuilder.verifyDatasetRowCount(1L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void testBuildDataset_mongo_notOpenStat() {
        DataBuilder dataBuilder = buildDataBuild(false);
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerBuilder.initMongo()) {
            // insert data
            dataBuilder.buildDatasource(1L);

            Long gid = dbOperator.fetchOne("select gid from kun_mt_dataset", rs -> rs.getLong(1));
            // pull dataset
            dataBuilder.buildDataset(gid);

            // verify
            containerBuilder.verifyDatasetRowCount(10L);
            containerBuilder.verifyDatasetStatsRowCount(0L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildDataset_mongo_openStat() {
        DataBuilder dataBuilder = buildDataBuild(true);
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerBuilder.initMongo()) {
            // insert data
            dataBuilder.buildDatasource(1L);

            Long gid = dbOperator.fetchOne("select gid from kun_mt_dataset", rs -> rs.getLong(1));
            // pull dataset
            dataBuilder.buildDataset(gid);

            // verify
            containerBuilder.verifyDatasetRowCount(10L);
            containerBuilder.verifyDatasetStatsRowCount(11L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildDataset_postgres() {
        DataBuilder dataBuilder = buildDataBuild(false);
        // start postgres container
        try (PostgreSQLContainer postgreSQLContainer = containerBuilder.initPostgres()) {
            // insert data
            dataBuilder.buildDatasource(1L);
            containerBuilder.verifyDatasetRowCount(1L);

            // pull dataset
            Long gid = dbOperator.fetchOne("select gid from kun_mt_dataset", rs -> rs.getLong(1));
            String dataStore = dbOperator.fetchOne("select data_store from kun_mt_dataset", rs -> rs.getString(1));

            // delete postgres table
            DatabaseOperator operator = new DatabaseOperator(JDBCClient.getDataSource(postgreSQLContainer.getJdbcUrl(),
                    postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword(), DatabaseType.POSTGRES));
            operator.update("DROP TABLE bar");

            // pull dataset
            dataBuilder.buildDataset(gid);
            containerBuilder.verifyDatasetRowCount(1L);
            dataBuilder.sweep();

            // verify
            containerBuilder.verifyDatasetRowCount(1L);
            containerBuilder.verifyDatasetStatsRowCount(0L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void testBuildDataset_es() {
        DataBuilder dataBuilder = buildDataBuild(false);
        // start es container
        try (ElasticsearchContainer elasticsearchContainer = containerBuilder.initEs()) {
            // execute biz logic
            dataBuilder.buildDatasource(1L);
            Long gid = dbOperator.fetchOne("select gid from kun_mt_dataset", rs -> rs.getLong(1));

            // pull dataset
            dataBuilder.buildDataset(gid);

            // verify
            containerBuilder.verifyDatasetRowCount(1L);
            containerBuilder.verifyDatasetStatsRowCount(0L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }


    private void initDataset(long gid) {
        dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name) VALUES(?, ?, ?, CAST(? AS JSONB), ?)",
                gid, "test_dataset", 1, dataStoreJson, "test_database");
    }

    private void verifyLatestStates(Map<String, Long> latestStates, long targetGid, String targetDataStore) {
        assertThat(latestStates.size(), is(1));
        for (Map.Entry<String, Long> entry : latestStates.entrySet()) {
            String dataStoreStr = entry.getKey();
            Long gid = entry.getValue();
            assertThat(dataStoreStr, is(targetDataStore));
            assertThat(gid, is(targetGid));
        }
    }

    private DataBuilder buildDataBuild(boolean extractStats) {
        Props props = new Props();
        props.put(OperatorKey.EXTRACT_STATS, String.valueOf(extractStats));
        return new DataBuilder(dbOperator, props);
    }

}
