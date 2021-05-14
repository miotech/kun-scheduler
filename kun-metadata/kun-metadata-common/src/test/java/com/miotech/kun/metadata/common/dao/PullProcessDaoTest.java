package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.process.PullDataSourceProcess;
import com.miotech.kun.metadata.core.model.process.PullProcess;
import org.junit.Test;

import java.time.OffsetDateTime;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;

public class PullProcessDaoTest extends DatabaseTestBase {

    @Inject
    private PullProcessDao pullProcessDao;

    @Test
    public void create_withValidProcessEntity_shouldWork() {
        OffsetDateTime now = OffsetDateTime.now();
        PullProcess pullDataSourceProcessToCreate = PullDataSourceProcess.newBuilder()
                .withProcessId(1234L)
                .withCreatedAt(now)
                .withDataSourceId("123")
                .withMceTaskRunId(5678L)
                .build();
        PullProcess createdProcess = pullProcessDao.create(pullDataSourceProcessToCreate);
        assertThat(createdProcess, sameBeanAs(pullDataSourceProcessToCreate));
    }
}
