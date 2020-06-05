package com.miotech.kun.metadata.client;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

public class PostgresLoaderTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    private Dataset dataset;

    {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();
        datasetBuilder.withName("datasetName")
                .withDatasetStat(new DatasetStat(100L, LocalDate.now()))
                .withFields(ImmutableList.of(new DatasetField("id", new DatasetFieldType(DatasetFieldType.convertRawType("int"), "int"), "自增id"),
                        new DatasetField("name", new DatasetFieldType(DatasetFieldType.convertRawType("string"), "string"), "姓名")))
                .withFieldStats(ImmutableList.of(new DatasetFieldStat("id", 2,  98, "admin", LocalDate.now()),
                        new DatasetFieldStat("name", 3, 67, "admin", LocalDate.now())))
                .withDataStore(new HiveTableStore("", "db1", "tb"));
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

    }

    @Test
    public void testLoad_rollback() {

    }

    @Test
    public void testLoad_updateField() {

    }
}
