package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.facade.backfill.Backfill;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockBackfillFactory {

    public static Backfill createBackfill() {
        return createBackfill(1).get(0);
    }

    public static Backfill createBackfill(long taskRunId) {
        return createBackfill(1, taskRunId).get(0);
    }

    public static List<Backfill> createBackfill(int size) {
        return createBackfill(size, 0);
    }

    public static List<Backfill> createBackfill(int size, long taskRunId) {
        return createBackfill(size, 0, taskRunId);
    }

    public static List<Backfill> createBackfill(int size, int indexOffset) {
        OffsetDateTime now = DateTimeUtils.now();
        List<Backfill> backfillList = new ArrayList<>();
        for (int i = indexOffset + 1; i <= size + indexOffset; ++i) {
            Backfill exampleBackfillInstance = Backfill.newBuilder()
                    .withId(100L + i)
                    .withName("example-backfill-" + i)
                    .withCreator("admin")
                    .withTaskRunIds(ImmutableList.of(1000L * i + 1, 1000L * i + 2, 1000L * i + 3))
                    .withWorkflowTaskIds(ImmutableList.of(101L, 102L, 103L))
                    .withTaskDefinitionIds(ImmutableList.of(1L, 2L, 3L))
                    .withCreateTime(now)
                    .withUpdateTime(now)
                    .build();
            backfillList.add(exampleBackfillInstance);
        }
        return backfillList;
    }

    public static List<Backfill> createBackfill(int size, int indexOffset, long taskRunId) {
        OffsetDateTime now = DateTimeUtils.now();
        List<Backfill> backfillList = new ArrayList<>();
        for (int i = indexOffset + 1; i <= size + indexOffset; ++i) {
            Backfill exampleBackfillInstance = Backfill.newBuilder()
                    .withId(100L + i)
                    .withName("example-backfill-" + i)
                    .withCreator("admin")
                    .withTaskRunIds(ImmutableList.of(taskRunId))
                    .withWorkflowTaskIds(ImmutableList.of(101L))
                    .withTaskDefinitionIds(ImmutableList.of(1L))
                    .withCreateTime(now)
                    .withUpdateTime(now)
                    .build();
            backfillList.add(exampleBackfillInstance);
        }
        return backfillList;
    }
}
