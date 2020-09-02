package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.databuilder.TestContainerUtil;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
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

    @Inject
    private DataBuilder dataBuilder;

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private TestContainerUtil containerUtil;

    private long gid = IdGenerator.getInstance().nextId();

    @Test
    public void testBuildAll() {
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerUtil.initMongo()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildAll();

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, "{}");

            dataBuilder.sweep();
            containerUtil.verifyDatasetRowCount(10L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }


    @Test
    public void testBuildDatasource_mongo() {
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerUtil.initMongo()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, "{}");

            dataBuilder.sweep();
            containerUtil.verifyDatasetRowCount(10L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildDatasource_postgres() {
        // start postgres container
        try (PostgreSQLContainer postgres = containerUtil.initPostgres()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, "{}");

            dataBuilder.sweep();
            containerUtil.verifyDatasetRowCount(1L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void testBuildDatasource_es() {
        // start es container
        try (ElasticsearchContainer elasticsearch = containerUtil.initEs()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, "{}");

            dataBuilder.sweep();
            containerUtil.verifyDatasetRowCount(1L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    @Ignore
    public void testBuildDatasource_arango() {
        // start arango container
        try (ArangoContainer arangoContainer = containerUtil.initArango()) {
            // init data
            initDataset(gid);

            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, "{}");

            dataBuilder.sweep();
            containerUtil.verifyDatasetRowCount(1L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void testBuildDataset_mongo() {
        // start postgres container
        try (MongoDBContainer mongoDBContainer = containerUtil.initMongo()) {
            // insert data
            dataBuilder.buildDatasource(1L);

            Long gid = dbOperator.fetchOne("select gid from kun_mt_dataset", rs -> rs.getLong(1));
            // pull dataset
            dataBuilder.buildDataset(gid);

            // verify
            containerUtil.verifyDatasetStatsRowCount(11L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildDataset_postgres() {
        // start postgres container
        try (PostgreSQLContainer postgreSQLContainer = containerUtil.initPostgres()) {
            // insert data
            dataBuilder.buildDatasource(1L);
            containerUtil.verifyDatasetRowCount(1L);

            // pull dataset
            Long gid = dbOperator.fetchOne("select gid from kun_mt_dataset", rs -> rs.getLong(1));
            String dataStore = dbOperator.fetchOne("select data_store from kun_mt_dataset", rs -> rs.getString(1));
            dataBuilder.buildDataset(gid);
            containerUtil.verifyDatasetStatsRowCount(2L);

            // delete postgres table
            DatabaseOperator operator = new DatabaseOperator(JDBCClient.getDataSource(postgreSQLContainer.getJdbcUrl(),
                    postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword(), DatabaseType.POSTGRES));
            operator.update("DROP TABLE bar");

            // pull dataset
            dataBuilder.buildDataset(gid);
            dataBuilder.sweep();

            // verify
            verifyLatestStates(dataBuilder.getLatestStates(), gid, dataStore);
            containerUtil.verifyDatasetRowCount(0L);
            containerUtil.verifyDatasetStatsRowCount(2L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void testBuildDataset_es() {
        // start es container
        try (ElasticsearchContainer elasticsearchContainer = containerUtil.initEs()) {
            // execute biz logic
            dataBuilder.buildDatasource(1L);
            Long gid = dbOperator.fetchOne("select gid from kun_mt_dataset", rs -> rs.getLong(1));

            // pull dataset
            dataBuilder.buildDataset(gid);

            // verify
            containerUtil.verifyDatasetStatsRowCount(2L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }


    private void initDataset(long gid) {
        dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name) VALUES(?, ?, ?, CAST(? AS JSONB), ?)",
                gid, "test_dataset", 1, "{}", "test_database");
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

}
