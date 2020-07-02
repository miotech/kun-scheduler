package com.miotech.kun.metadata.databuilder.load;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.databuilder.load.impl.PostgresLoader;
import com.miotech.kun.metadata.databuilder.model.*;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
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
                .withDatasourceId(1L)
                .withDatasetStat(new DatasetStat(100L, LocalDateTime.now()))
                .withFields(ImmutableList.of(new DatasetField("id", new DatasetFieldType(DatasetFieldType.convertRawType("int"), "int"), "auto increment"),
                        new DatasetField("name", new DatasetFieldType(DatasetFieldType.convertRawType("string"), "string"), "test name")))
                .withFieldStats(ImmutableList.of(new DatasetFieldStat("id", 2,  98, "admin", LocalDateTime.now()),
                        new DatasetFieldStat("name", 3, 67, "admin", LocalDateTime.now())))
                .withDataStore(new HiveTableStore("", "test_database", "test_table"));
        dataset = datasetBuilder.build();
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
            dataset = dataset.cloneBuilder().withDatasourceId(1L)
                    .withDatasetStat(new DatasetStat(100L, null))
                    .withFieldStats(ImmutableList.of(new DatasetFieldStat("id", 2,  98, "admin", LocalDateTime.now()),
                            new DatasetFieldStat("name", 3, 167, "admin", LocalDateTime.now()))).build();
            postgresLoader.load(dataset);
        } catch (Exception e) {
            MatcherAssert.assertThat(e, Matchers.instanceOf(RuntimeException.class));
        }
        rowCount = operator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset", rs -> rs.getLong(1));
        Assert.assertEquals(rowCount, Long.valueOf(0));
    }

    @Test
    public void testLoad_updateField() {
        PostgresLoader postgresLoader = new PostgresLoader(operator);
        operator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, `name`, `type`, raw_type) VALUES(?, ?, ?, ?)", 100L, "age", "NUMBER", "int");
        operator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, `name`, `type`, raw_type) VALUES(?, ?, ?, ?)", 100L, "id", "CHARACTER", "string");
        Long id = operator.fetchOne("SELECT id FROM kun_mt_dataset_field WHERE dataset_gid = 100 AND `name` = 'age'", rs -> rs.getLong(1));
        Assert.assertNotNull(id);

        GidService mockGidService = Mockito.mock(GidService.class);
        Mockito.when(mockGidService.generate(dataset.getDataStore())).thenReturn(100L);

        Reflect.on(postgresLoader).set("gidGenerator", mockGidService);

        postgresLoader.load(dataset);
        List<String> fieldNames = operator.fetchAll("SELECT `name` FROM kun_mt_dataset_field WHERE dataset_gid = 100", rs -> rs.getString(1));
        List<String> types = operator.fetchAll("SELECT `type` FROM kun_mt_dataset_field WHERE dataset_gid = 100", rs -> rs.getString(1));
        assertThat(fieldNames, containsInAnyOrder("id", "name"));
        assertThat(types, containsInAnyOrder("NUMBER", "CHARACTER"));
        id = operator.fetchOne("SELECT id FROM kun_mt_dataset_field WHERE dataset_gid = 100 AND `name` = 'age'", rs -> rs.getLong(1));
        Assert.assertNull(id);
    }
}
