package com.miotech.kun.metadata.databuilder.load;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.dao.MetadataDatasetDao;
import com.miotech.kun.metadata.core.model.*;
import com.miotech.kun.metadata.databuilder.load.impl.PostgresLoader;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.apache.logging.log4j.util.Strings;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PostgresLoaderTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Inject
    private PostgresLoader postgresLoader;

    @Inject
    private MetadataDatasetDao metadataDatasetDao;

    @Test
    public void testLoadSchema() {
        long gid = IdGenerator.getInstance().nextId();

        Dataset dataset = Dataset.newBuilder()
                .withName("datasetName")
                .withDatasourceId(1L)
                .withFields(ImmutableList.of(new DatasetField("id", new DatasetFieldType(DatasetFieldType.convertRawType("int"), "int"), "auto increment", false, false),
                        new DatasetField("name", new DatasetFieldType(DatasetFieldType.convertRawType("string"), "string"), "test name", false, true)))
                .withDataStore(new HiveTableStore("", "test_database", "test_table"))
                .build();

        GidService mockGidService = Mockito.mock(GidService.class);
        Mockito.when(mockGidService.generate(dataset.getDataStore())).thenReturn(gid);
        Reflect.on(postgresLoader).set("gidGenerator", mockGidService);
        postgresLoader.loadSchema(dataset);

        Optional<Dataset> datasetOpt = metadataDatasetDao.fetchDatasetByGid(gid);
        Assert.assertTrue(datasetOpt.isPresent());
        MatcherAssert.assertThat(datasetOpt.get().getName(), Matchers.is(dataset.getName()));
        MatcherAssert.assertThat(datasetOpt.get().getFields().size(), Matchers.is(2));

        List<DatasetField> fields = Collections.singletonList(DatasetField.newBuilder()
                .withFieldType(new DatasetFieldType(DatasetFieldType.convertRawType("int"), "int"))
                .withComment(Strings.EMPTY)
                .withName("id")
                .withIsPrimaryKey(true)
                .withIsNullable(true)
                .build());
        postgresLoader.loadSchema(gid, fields);

        datasetOpt = metadataDatasetDao.fetchDatasetByGid(gid);
        Assert.assertTrue(datasetOpt.isPresent());
        Dataset datasetResult = datasetOpt.get();
        MatcherAssert.assertThat(datasetResult.getName(), Matchers.is(dataset.getName()));
        MatcherAssert.assertThat(datasetResult.getFields().size(), Matchers.is(1));
        DatasetField datasetField = datasetResult.getFields().get(0);
        MatcherAssert.assertThat(datasetField.isPrimaryKey(), Matchers.is(true));
        MatcherAssert.assertThat(datasetField.isNullable(), Matchers.is(true));
    }

    @Test
    public void testLoadStat() {
        long gid = IdGenerator.getInstance().nextId();
        long rowCount = 100L;
        long distinctCount = 80;
        long nonnullCount = 90;

        List<DatasetFieldStat> fieldStats = Collections.singletonList(DatasetFieldStat.newBuilder()
                .withFieldName("id")
                .withStatDate(LocalDateTime.now())
                .withDistinctCount(distinctCount)
                .withNonnullCount(nonnullCount)
                .build());

        Dataset dataset = Dataset.newBuilder()
                .withGid(gid)
                .withDatasetStat(TableStatistics.newBuilder()
                        .withLastUpdatedTime(LocalDateTime.now())
                        .withStatDate(LocalDateTime.now())
                        .withRowCount(rowCount)
                        .withTotalByteSize(1024L)
                        .build())
                .withFieldStats(fieldStats)
                .build();

        postgresLoader.loadStatistics(dataset);

        Long rowCountResult = operator.fetchOne("SELECT row_count FROM kun_mt_dataset_stats WHERE dataset_gid = ?", rs -> rs.getLong(1), gid);
        MatcherAssert.assertThat(rowCountResult, Matchers.is(rowCount));

        Long totalByteSize = operator.fetchOne("SELECT total_byte_size FROM kun_mt_dataset_stats WHERE dataset_gid = ?", rs -> rs.getLong(1), gid);
        MatcherAssert.assertThat(totalByteSize, Matchers.is(1024L));
    }

}