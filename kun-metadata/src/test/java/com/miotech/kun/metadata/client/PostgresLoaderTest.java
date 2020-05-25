package com.miotech.kun.metadata.client;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.metadata.load.impl.PostgresLoader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.workflow.core.model.entity.DataStore;
import com.miotech.kun.workflow.core.model.entity.HiveCluster;
import com.miotech.kun.workflow.core.model.entity.HiveTableStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class PostgresLoaderTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    private Dataset dataset;

    {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();
        datasetBuilder.withName("datasetName")
                .withDatasetStat(new DatasetStat(100L, new Date()))
                .withFields(ImmutableList.of(new DatasetField("id", "bigint", "自增id"),
                        new DatasetField("name", "string", "姓名")))
                .withFieldStats(ImmutableList.of(new DatasetFieldStat("id", 2,  98, "admin", new Date()),
                        new DatasetFieldStat("name", 3, 67, "admin", new Date())))
                .withDataStore(new HiveTableStore("db1", "tb", new HiveCluster(123L, "abc", "123", "sdf", "dsf", "sdf", "wer")));
        dataset = datasetBuilder.build();
    }

    @Before
    public void createTable() {
        operator.update("CREATE TABLE `kun_mt_dataset` (\n" +
                "  `gid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(1024) DEFAULT NULL,\n" +
                "  `cluster_id` bigint(20) DEFAULT NULL,\n" +
                "  `data_store` varchar(1024) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`gid`)\n" +
                ")");
        operator.update("CREATE TABLE `kun_mt_dataset_field` (\n" +
                "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `dataset_gid` bigint(20) DEFAULT NULL,\n" +
                "  `name` varchar(1024) DEFAULT NULL,\n" +
                "  `type` varchar(64) DEFAULT NULL,\n" +
                "  `description` varchar(1024) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ")");
        operator.update("CREATE TABLE `kun_mt_dataset_field_stats` (\n" +
                "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `field_id` bigint(20) DEFAULT NULL,\n" +
                "  `stats_date` datetime DEFAULT NULL,\n" +
                "  `distinct_count` bigint(20) DEFAULT NULL,\n" +
                "  `nonnull_count` bigint(20) DEFAULT NULL,\n" +
                "  `nonnull_percentage` decimal(12,4) DEFAULT NULL,\n" +
                "  `updator` varchar(1024) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ")");
        operator.update("CREATE TABLE `kun_mt_dataset_gid` (\n" +
                "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `data_store` varchar(1024) DEFAULT NULL,\n" +
                "  `dataset_gid` bigint(20) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ")");
        operator.update("CREATE TABLE `kun_mt_dataset_stats` (\n" +
                "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `dataset_gid` bigint(20) DEFAULT NULL,\n" +
                "  `stats_date` datetime DEFAULT NULL,\n" +
                "  `row_count` bigint(20) DEFAULT NULL,\n" +
                "  `updator` varchar(1024) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ")");
    }

    @Test
    public void testLoad_commit() {
        PostgresLoader postgresLoader = new PostgresLoader(operator);

        Long rowCount = operator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset", rs -> rs.getLong(1));
        Assert.assertEquals(rowCount, Long.valueOf(0));
        postgresLoader.load(dataset);
        rowCount = operator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset", rs -> rs.getLong(1));
        Assert.assertEquals(rowCount, Long.valueOf(1));

    }

    @Test
    public void testLoad_rollback() {
        PostgresLoader postgresLoader = new PostgresLoader(operator);

        Long rowCount = operator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset", rs -> rs.getLong(1));
        Assert.assertEquals(rowCount, Long.valueOf(0));

        try {
            dataset = dataset.cloneBuilder().withDatasetStat(new DatasetStat(100L, new Date()))
                    .withFieldStats(ImmutableList.of(new DatasetFieldStat("id", 2,  98, "admin", new Date()),
                            new DatasetFieldStat("name", 3, 167, "admin", new Date()))).build();
            postgresLoader.load(dataset);
        } catch (RuntimeException e) {
        }
        rowCount = operator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset", rs -> rs.getLong(1));
        Assert.assertEquals(rowCount, Long.valueOf(0));

    }
}
