package com.miotech.kun.dataquality;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.entity.CaseRun;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DataQualityRepositoryTest extends DataQualityTestBase {

    @Autowired
    private DataQualityRepository dataQualityRepository;

    @Test
    public void insertCaseRunWithTaskRun() {
        //prepare
        List<Long> caseRunIdList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            caseRunIdList.add(IdGenerator.getInstance().nextId());
        }
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        Long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        List<CaseRun> caseRunList = new ArrayList<>();
        for (int i = 0; i < caseRunIdList.size(); i++) {
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(caseRunIdList.get(i));
            caseRun.setTaskRunId(taskRunId);
            caseRun.setTaskAttemptId(taskAttemptId);
            caseRun.setCaseId(IdGenerator.getInstance().nextId());
            caseRun.setValidateDatasetId(IdGenerator.getInstance().nextId());
            caseRunList.add(caseRun);
        }
        dataQualityRepository.insertCaseRunWithTaskRun(caseRunList);

        //verify
        for (Long caseRunId : caseRunIdList) {
            Long savedTaskRunId = dataQualityRepository.fetchTaskRunIdByCase(caseRunId);
            String caseRunStatus = dataQualityRepository.fetchCaseRunStatus(caseRunId);
            assertThat(savedTaskRunId, is(taskRunId));
            assertThat(caseRunStatus, is(DataQualityStatus.CREATED.name()));
        }

    }

}
