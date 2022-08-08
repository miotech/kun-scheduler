package com.miotech.kun.dataquality.service;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.DataQualityTestBase;
import com.miotech.kun.dataquality.mock.MockAbnormalDatasetFactory;
import com.miotech.kun.dataquality.mock.MockTaskRunFactory;
import com.miotech.kun.dataquality.web.model.AbnormalDataset;
import com.miotech.kun.dataquality.web.persistence.AbnormalDatasetRepository;
import com.miotech.kun.dataquality.web.service.AbnormalDatasetService;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbnormalDatasetServiceTest extends DataQualityTestBase {

    @Autowired
    private AbnormalDatasetService abnormalDatasetService;

    @Autowired
    private AbnormalDatasetRepository abnormalDatasetRepository;

    @Test
    public void testCreate() {
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create();
        Long datasetGid = IdGenerator.getInstance().nextId();
        AbnormalDataset created = abnormalDatasetService.create(abnormalDataset, datasetGid);

        List<AbnormalDataset> abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        assertThat(abnormalDatasets.get(0), sameBeanAs(created).ignoring("id").ignoring("datasetGid"));
        assertThat(abnormalDatasets.get(0).getDatasetGid(), is(datasetGid));
        assertThat(abnormalDatasets.get(0).getScheduleAt(), is(created.getScheduleAt()));
    }

    @Test
    public void testFetchByScheduleAtAndStatusIsNull() {
        // create abnormal dataset
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create();
        Long datasetGid = IdGenerator.getInstance().nextId();
        AbnormalDataset created = abnormalDatasetService.create(abnormalDataset, datasetGid);

        List<AbnormalDataset> abnormalDatasets = abnormalDatasetService.fetchByScheduleAtAndStatusIsNull(created.getScheduleAt());
        assertThat(abnormalDatasets.size(), is(1));
        assertThat(abnormalDatasets.get(0), sameBeanAs(created).ignoring("id").ignoring("datasetGid"));
        assertThat(abnormalDatasets.get(0).getDatasetGid(), is(datasetGid));
        assertThat(abnormalDatasets.get(0).getScheduleAt(), is(created.getScheduleAt()));
    }

    @Test
    public void testUpdateStatus_notExist() {
        TaskRun taskRun = MockTaskRunFactory.create(TaskRunStatus.SUCCESS);
        boolean result = abnormalDatasetService.updateStatus(1L, taskRun);
        assertFalse(result);
    }

    @Test
    public void testUpdateStatus_createThenUpdate() {
        // create abnormal dataset
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create();
        Long datasetGid = IdGenerator.getInstance().nextId();
        abnormalDatasetService.create(abnormalDataset, datasetGid);

        List<AbnormalDataset> abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        TaskRun taskRun = MockTaskRunFactory.create(TaskRunStatus.SUCCESS);
        boolean result = abnormalDatasetService.updateStatus(abnormalDatasets.get(0).getId(), taskRun);
        assertTrue(result);
    }

    @Test
    public void testUpdateStatusByTaskRunId() {
        // create abnormal dataset
        AbnormalDataset abnormalDataset = MockAbnormalDatasetFactory.create();
        Long datasetGid = IdGenerator.getInstance().nextId();
        abnormalDatasetService.create(abnormalDataset, datasetGid);

        List<AbnormalDataset> abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        assertThat(abnormalDatasets.get(0).getStatus(), nullValue());

        // execute
        abnormalDatasetService.updateStatusByTaskRunId(abnormalDataset.getTaskRunId(), "SUCCESS");

        // validate
        abnormalDatasets = abnormalDatasetRepository.fetchAll();
        assertThat(abnormalDatasets.size(), is(1));
        assertThat(abnormalDatasets.get(0).getStatus(), is("SUCCESS"));
    }

}
