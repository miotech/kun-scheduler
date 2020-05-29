package com.miotech.kun.metadata.client;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.metadata.load.impl.PostgresLoader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.metadata.service.gid.GidService;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class PostgresLoaderTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    private Dataset dataset;

    {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();
        datasetBuilder.withName("datasetName")
                .withDatasetStat(new DatasetStat(100L, new Date()))
                .withFields(ImmutableList.of(new DatasetField("id", "def", "自增id"),
                        new DatasetField("name", "string", "姓名")))
                .withFieldStats(ImmutableList.of(new DatasetFieldStat("id", 2,  98, "admin", new Date()),
                        new DatasetFieldStat("name", 3, 67, "admin", new Date())))
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
            MatcherAssert.assertThat(e.getMessage(), Matchers.startsWith("logic exception(nonnullCount > rowCount)"));
        }
        rowCount = operator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset", rs -> rs.getLong(1));
        Assert.assertEquals(rowCount, Long.valueOf(0));

    }

    @Test
    public void testLoad_updateField() {
        PostgresLoader postgresLoader = new PostgresLoader(operator);
        operator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, `name`, `type`) VALUES(?, ?, ?)", 100L, "age", "int");
        operator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, `name`, `type`) VALUES(?, ?, ?)", 100L, "id", "abc");
        Long id = operator.fetchOne("SELECT id FROM kun_mt_dataset_field WHERE dataset_gid = 100 AND `name` = 'age'", rs -> rs.getLong(1));
        Assert.assertNotNull(id);

        GidService mockGidService = Mockito.mock(GidService.class);
        Mockito.when(mockGidService.generate(dataset.getDataStore())).thenReturn(100L);

        Reflect.on(postgresLoader).set("gidGenerator", mockGidService);

        postgresLoader.load(dataset);
        List<String> fieldNames = operator.fetchAll("SELECT `name` FROM kun_mt_dataset_field WHERE dataset_gid = 100", rs -> rs.getString(1));
        List<String> types = operator.fetchAll("SELECT `type` FROM kun_mt_dataset_field WHERE dataset_gid = 100", rs -> rs.getString(1));
        assertThat(fieldNames, containsInAnyOrder("id", "name"));
        assertThat(types, containsInAnyOrder("def", "string"));
        id = operator.fetchOne("SELECT id FROM kun_mt_dataset_field WHERE dataset_gid = 100 AND `name` = 'age'", rs -> rs.getLong(1));
        Assert.assertNull(id);
    }
}
