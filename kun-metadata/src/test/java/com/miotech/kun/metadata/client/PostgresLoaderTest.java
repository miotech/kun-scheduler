package com.miotech.kun.metadata.client;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;

@Ignore
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
