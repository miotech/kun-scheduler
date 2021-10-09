package com.miotech.kun.metadata.common.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.process.PullDataSourceProcess;
import com.miotech.kun.metadata.core.model.process.PullDatasetProcess;
import com.miotech.kun.metadata.core.model.process.PullProcess;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class PullProcessDaoTest extends DatabaseTestBase {

    @Inject
    private PullProcessDao pullProcessDao;

    @Test
    public void create_withValidProcessEntity_shouldWork() {
        // 1. Prepare
        OffsetDateTime now = DateTimeUtils.now();
        PullProcess pullDataSourceProcessToCreate = PullDataSourceProcess.newBuilder()
                .withProcessId(1234L)
                .withCreatedAt(now)
                .withDataSourceId(123L)
                .withMceTaskRunId(5678L)
                .build();
        PullProcess pullDatasetProcessToCreate = PullDatasetProcess.newBuilder()
                .withProcessId(null)
                .withCreatedAt(now)
                .withDatasetId(234L)
                .withMceTaskRunId(7890L)
                .withMseTaskRunId(null)
                .build();

        // 2. Process
        PullProcess createdProcess1 = pullProcessDao.create(pullDataSourceProcessToCreate);
        PullProcess createdProcess2 = pullProcessDao.create(pullDatasetProcessToCreate);

        // 3. Validate
        assertThat(createdProcess1, sameBeanAs(pullDataSourceProcessToCreate));
        assertThat(createdProcess2, sameBeanAs(pullDatasetProcessToCreate).ignoring("processId").ignoring("mseTaskRunId"));
    }

    @Test
    public void create_withInvalidProcessEntity_shouldThrowException() {
        // 1. Prepare
        OffsetDateTime now = DateTimeUtils.now();
        PullProcess pullDataSourceProcessWithInvalidProps = PullDataSourceProcess.newBuilder()
                .withProcessId(null)
                .withCreatedAt(now)
                .withDataSourceId(null)
                .withMceTaskRunId(5678L)
                .build();
        PullProcess pullDatasetProcessWithInvalidProps = PullDatasetProcess.newBuilder()
                .withProcessId(null)
                .withCreatedAt(now)
                .withDatasetId(null)
                .withMceTaskRunId(5678L)
                .withMseTaskRunId(null)
                .build();

        // 2. Process & Validate
        try {
            pullProcessDao.create(pullDataSourceProcessWithInvalidProps);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }

        try {
            pullProcessDao.create(pullDatasetProcessWithInvalidProps);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void findLatestDataSourcePullProcess_withMultipleInstances_shouldReturnTheLatestOne() {
        // 1. Prepare
        // time3 > time2 > time1
        OffsetDateTime time1 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394162308L)));
        OffsetDateTime time2 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394197304L)));
        OffsetDateTime time3 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394212846L)));
        final Long dataSourceId = 123L;

        PullProcess pullDataSourceProcess1 = newPullDataSourceProcess(dataSourceId, time1);
        PullProcess pullDataSourceProcess2 = newPullDataSourceProcess(dataSourceId, time2);
        PullProcess pullDataSourceProcess3 = newPullDataSourceProcess(dataSourceId, time3);

        // 2. Process
        pullProcessDao.create(pullDataSourceProcess1);
        pullProcessDao.create(pullDataSourceProcess2);
        pullProcessDao.create(pullDataSourceProcess3);

        Optional<PullDataSourceProcess> processLatest = pullProcessDao.findLatestPullDataSourceProcessByDataSourceId(dataSourceId);

        // 3. Validate
        assertTrue(processLatest.isPresent());
        assertThat(processLatest.get().getProcessId(), is(pullDataSourceProcess3.getProcessId()));
    }

    @Test
    public void findLatestDataSourcePullProcess_withNoInstance_shouldReturnNullOptional() {
        // 1. Process
        // Try to fetch process of a data source that never pulled
        Optional<PullDataSourceProcess> processLatest = pullProcessDao.findLatestPullDataSourceProcessByDataSourceId(1231243L);

        // 2. Validate
        assertFalse(processLatest.isPresent());
    }

    @Test
    public void findLatestDataSetPullProcess_withMultipleInstances_shouldReturnTheLatestOne() {
        // 1. Prepare
        // time3 > time2 > time1
        OffsetDateTime time1 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394162308L)));
        OffsetDateTime time2 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394197304L)));
        OffsetDateTime time3 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394212846L)));
        final Long datasetId = 456L;

        PullProcess pullDatasetProcess1 = newPullDatasetProcess(datasetId, time1);
        PullProcess pullDatasetProcess2 = newPullDatasetProcess(datasetId, time2);
        PullProcess pullDatasetProcess3 = newPullDatasetProcess(datasetId, time3);

        // 2. Process
        pullProcessDao.create(pullDatasetProcess1);
        pullProcessDao.create(pullDatasetProcess2);
        pullProcessDao.create(pullDatasetProcess3);

        Optional<PullDatasetProcess> processLatest = pullProcessDao.findLatestPullDatasetProcessByDataSetId(datasetId);

        // 3. Validate
        assertTrue(processLatest.isPresent());
        assertThat(processLatest.get().getProcessId(), is(pullDatasetProcess3.getProcessId()));
    }

    @Test
    public void findLatestDatasetPullProcess_withNoInstance_shouldReturnNullOptional() {
        // 1. Process
        // Try to fetch process of a data source that never pulled
        Optional<PullDatasetProcess> processLatest = pullProcessDao.findLatestPullDatasetProcessByDataSetId(1231243L);

        // 2. Validate
        assertFalse(processLatest.isPresent());
    }

    @Test
    public void findLatestPullDataSourceProcessesByDataSourceIds_withMultipleDataSourceIds_shouldReturnLatestProcessOfEach() {
        // 1. Prepare
        // time5 > time4 > time3 > time2 > time1
        OffsetDateTime time1 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394162308L)));
        OffsetDateTime time2 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394197304L)));
        OffsetDateTime time3 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394212846L)));
        OffsetDateTime time4 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394212946L)));
        OffsetDateTime time5 = DateTimeUtils.fromTimestamp(Timestamp.from(Instant.ofEpochMilli(1621394213046L)));

        final Long dataSource1Id = 1001L;
        final Long dataSource2Id = 1002L;
        final Long dataSource3Id = 1003L;
        final Long dataSource4Id = 1004L;

        // data source 1 has 3 pull processes
        PullProcess process1_1 = newPullDataSourceProcess(dataSource1Id, time1);
        PullProcess process1_2 = newPullDataSourceProcess(dataSource1Id, time2);
        PullProcess process1_3 = newPullDataSourceProcess(dataSource1Id, time3);
        // data source 2 has 2 pull processes
        PullProcess process2_1 = newPullDataSourceProcess(dataSource2Id, time4);
        PullProcess process2_2 = newPullDataSourceProcess(dataSource2Id, time5);
        // data source 3 has 1 pull process
        PullProcess process3_1 = newPullDataSourceProcess(dataSource3Id, time2);
        // data source 4 was never pulled

        PullProcess[] processesToCreate = {process1_1, process1_2, process1_3, process2_2, process2_1, process3_1};

        // 2. Process
        for (PullProcess process : processesToCreate) {
            pullProcessDao.create(process);
        }
        Map<Long, PullDataSourceProcess> latestProcessMaps =
                pullProcessDao.findLatestPullDataSourceProcessesByDataSourceIds(Lists.newArrayList(dataSource1Id, dataSource2Id, dataSource3Id, dataSource4Id));

        // Validate
        assertThat(latestProcessMaps.size(), is(3));
        assertTrue(latestProcessMaps.containsKey(dataSource1Id));
        assertTrue(latestProcessMaps.containsKey(dataSource2Id));
        assertTrue(latestProcessMaps.containsKey(dataSource3Id));
        // data source 4 is not included since it was never pulled
        assertFalse(latestProcessMaps.containsKey(dataSource4Id));

        assertThat(latestProcessMaps.get(dataSource1Id), sameBeanAs(process1_3));
        assertThat(latestProcessMaps.get(dataSource2Id), sameBeanAs(process2_2));
        assertThat(latestProcessMaps.get(dataSource3Id), sameBeanAs(process3_1));
    }

    private PullDataSourceProcess newPullDataSourceProcess(Long dataSourceId, OffsetDateTime createdAt) {
        return PullDataSourceProcess.newBuilder()
                .withProcessId(IdGenerator.getInstance().nextId())
                .withCreatedAt(createdAt)
                .withDataSourceId(dataSourceId)
                .withMceTaskRunId(IdGenerator.getInstance().nextId())
                .build();
    }

    private PullDatasetProcess newPullDatasetProcess(Long datasetId, OffsetDateTime createdAt) {
        return PullDatasetProcess.newBuilder()
                .withProcessId(IdGenerator.getInstance().nextId())
                .withCreatedAt(createdAt)
                .withDatasetId(datasetId)
                .withMceTaskRunId(IdGenerator.getInstance().nextId())
                .build();
    }
}
