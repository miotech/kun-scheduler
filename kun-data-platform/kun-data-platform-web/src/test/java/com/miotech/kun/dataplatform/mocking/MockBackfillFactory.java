package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockBackfillFactory {

    public static Backfill createBackfill() {
        return createBackfill(1).get(0);
    }

    public static List<Backfill> createBackfill(int size) {
        OffsetDateTime now = DateTimeUtils.now();
        List<Backfill> backfillList = new ArrayList<>();
        for (int i = 1; i <= size; ++i) {
            Backfill exampleBackfillInstance = Backfill.newBuilder()
                    .withId(100L + i)
                    .withName("example-backfill-" + i)
                    .withCreator(1L)
                    .withTaskRunIds(ImmutableList.of(1000L * i + 1, 1000L * i + 2, 1000L * i + 3))
                    .withTaskDefinitionIds(ImmutableList.of(1L, 2L, 3L))
                    .withCreateTime(now)
                    .withUpdateTime(now)
                    .build();
            backfillList.add(exampleBackfillInstance);
        }
        return backfillList;
    }
}
