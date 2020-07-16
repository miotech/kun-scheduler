package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.TestContainerUtil;
import com.miotech.kun.commons.db.DatabaseOperator;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class DataBuilderTest extends DatabaseTestBase {

    @Inject
    private DataBuilder dataBuilder;

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private TestContainerUtil containerUtil;

    @Test
    public void testBuildAll() {
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerUtil.initMongo()) {
            // execute biz logic
            dataBuilder.buildAll();

            // verify
            containerUtil.verifyDatasetRowCount(10L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildDatasource_mongo() {
        // start mongo container
        try (MongoDBContainer mongoDBContainer = containerUtil.initMongo()) {
            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            containerUtil.verifyDatasetRowCount(10L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    @Test
    public void testBuildDatasource_postgres() {
        // start postgres container
        try (PostgreSQLContainer postgres = containerUtil.initPostgres()) {
            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
            containerUtil.verifyDatasetRowCount(1L);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void testBuildDatasource_es() {
        // start es container
        try (ElasticsearchContainer elasticsearch = containerUtil.initEs();) {
            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
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
            // execute biz logic
            dataBuilder.buildDatasource(1L);

            // verify
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

            Long gid = dbOperator.fetchOne("select gid from kun_mt_dataset", rs -> rs.getLong(1));
            // pull dataset
            dataBuilder.buildDataset(gid);

            // verify
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

}
