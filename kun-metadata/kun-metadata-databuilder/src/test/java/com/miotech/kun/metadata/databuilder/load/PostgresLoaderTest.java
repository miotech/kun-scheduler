package com.miotech.kun.metadata.databuilder.load;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.*;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.junit.Test;

import java.time.LocalDateTime;

public class PostgresLoaderTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    private Dataset dataset;

    {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();
        datasetBuilder.withName("datasetName")
                .withDatasourceId(1L)
                .withDatasetStat(new DatasetStat(100L, LocalDateTime.now(), LocalDateTime.now()))
                .withFields(ImmutableList.of(new DatasetField("id", new DatasetFieldType(DatasetFieldType.convertRawType("int"), "int"), "auto increment"),
                        new DatasetField("name", new DatasetFieldType(DatasetFieldType.convertRawType("string"), "string"), "test name")))
                .withFieldStats(ImmutableList.of(new DatasetFieldStat("id", 2,  98, "admin", LocalDateTime.now()),
                        new DatasetFieldStat("name", 3, 67, "admin", LocalDateTime.now())))
                .withDataStore(new HiveTableStore("", "test_database", "test_table"));
        dataset = datasetBuilder.withGid(1L).build();
    }

    @Test
    public void testLoad_commit() {

    }

}