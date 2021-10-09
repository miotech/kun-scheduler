package com.miotech.kun.metadata.common.factory;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.dataset.*;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

public class MockDatasetSnapshotFactory {
    public static DatasetSnapshot create() {
        SchemaSnapshot schemaSnapshot = SchemaSnapshot.newBuilder()
                .withFields(ImmutableList.of(SchemaSnapshot.Schema.newBuilder()
                        .withName("test_field")
                        .withType("NUMBER")
                        .withRawType("int")
                        .withIsPrimaryKey(false)
                        .withIsNullable(true)
                        .withDescription("test description")
                        .build()))
                .build();

        StatisticsSnapshot statisticsSnapshot = new StatisticsSnapshot(TableStatistics.newBuilder()
                .withRowCount(1L)
                .withTotalByteSize(1L)
                .withStatDate(DateTimeUtils.now())
                .withLastUpdatedTime(DateTimeUtils.now())
                .build(),
                ImmutableList.of(FieldStatistics.newBuilder()
                        .withFieldName("test_field")
                        .withDistinctCount(1L)
                        .withNonnullCount(1L)
                        .withStatDate(DateTimeUtils.now())
                        .withUpdatedBy("admin")
                        .build()));

        return DatasetSnapshot.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withDatasetGid(IdGenerator.getInstance().nextId())
                .withSchemaSnapshot(schemaSnapshot)
                .withStatisticsSnapshot(statisticsSnapshot)
                .withSchemaAt(DateTimeUtils.now())
                .withStatisticsAt(DateTimeUtils.now())
                .build();
    }
}
