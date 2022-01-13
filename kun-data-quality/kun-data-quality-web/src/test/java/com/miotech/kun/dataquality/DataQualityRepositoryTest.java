package com.miotech.kun.dataquality;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.mock.MockDataQualityFactory;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.bo.DataQualityRequest;
import com.miotech.kun.dataquality.web.model.entity.CaseRun;
import com.miotech.kun.dataquality.web.model.entity.DataQualityCaseBasic;
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
    public void fetchCaseBasicByTaskId() {
        //prepare
        DataQualityRequest dataQualityRequest = MockDataQualityFactory.createRequest();
        dataQualityRepository.addCase(dataQualityRequest);

        DataQualityCaseBasic savedBasic = dataQualityRepository.fetchCaseBasicByTaskId(dataQualityRequest.getTaskId());

        //verify
        assertThat(savedBasic.getTaskId(), is(dataQualityRequest.getTaskId()));
        assertThat(savedBasic.getName(), is(dataQualityRequest.getName()));
        assertThat(savedBasic.getIsBlocking(), is(dataQualityRequest.getIsBlocking()));

    }

    @Test
    public void updateCaseIsBlocking() {
        //prepare
        DataQualityRequest dataQualityRequest = MockDataQualityFactory.createRequest();
        dataQualityRequest.setIsBlocking(true);
        Long caseId = dataQualityRepository.addCase(dataQualityRequest);

        DataQualityCaseBasic savedBasic = dataQualityRepository.fetchCaseBasicByTaskId(dataQualityRequest.getTaskId());

        //verify
        assertThat(savedBasic.getTaskId(), is(dataQualityRequest.getTaskId()));
        assertThat(savedBasic.getName(), is(dataQualityRequest.getName()));
        assertThat(savedBasic.getIsBlocking(), is(true));

        dataQualityRequest.setIsBlocking(false);
        dataQualityRepository.updateCase(caseId,dataQualityRequest);
        savedBasic = dataQualityRepository.fetchCaseBasicByTaskId(dataQualityRequest.getTaskId());

        //verify
        assertThat(savedBasic.getTaskId(), is(dataQualityRequest.getTaskId()));
        assertThat(savedBasic.getName(), is(dataQualityRequest.getName()));
        assertThat(savedBasic.getIsBlocking(), is(false));



    }

    @Test
    public void validateTaskRunTestCase_all_pass_should_return_ture() {
        //prepare
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        List<CaseRun> caseRunList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DataQualityRequest dataQualityRequest = MockDataQualityFactory.createRequest();
            Long caseId = dataQualityRepository.addCase(dataQualityRequest);
            Long caseRunId = IdGenerator.getInstance().nextId();
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(caseRunId);
            caseRun.setTaskRunId(taskRunId);
            caseRun.setCaseId(caseId);
            caseRunList.add(caseRun);
        }

        dataQualityRepository.insertCaseRunWithTaskRun(caseRunList);

        DataQualityStatus testCasePass = dataQualityRepository.validateTaskRunTestCase(taskRunId);
        assertThat(testCasePass, is(DataQualityStatus.CREATED));

        //verify
        for (CaseRun caseRun : caseRunList) {
            dataQualityRepository.updateCaseRunStatus(caseRun.getCaseRunId(), true);
        }
        testCasePass = dataQualityRepository.validateTaskRunTestCase(taskRunId);
        assertThat(testCasePass, is(DataQualityStatus.SUCCESS));
    }

    @Test
    public void validateTaskRunTestCase_any_not_success_should_return_false() {
        //prepare
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        List<CaseRun> caseRunList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DataQualityRequest dataQualityRequest = MockDataQualityFactory.createRequest();
            Long caseId = dataQualityRepository.addCase(dataQualityRequest);
            Long caseRunId = IdGenerator.getInstance().nextId();
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(caseRunId);
            caseRun.setTaskRunId(taskRunId);
            caseRun.setCaseId(caseId);
            caseRunList.add(caseRun);
        }
        dataQualityRepository.insertCaseRunWithTaskRun(caseRunList);

        DataQualityStatus testCasePass = dataQualityRepository.validateTaskRunTestCase(taskRunId);
        assertThat(testCasePass, is(DataQualityStatus.CREATED));

        //verify
        for (int i = 0; i < 4; i++) {
            dataQualityRepository.updateCaseRunStatus(caseRunList.get(i).getCaseRunId(), true);
            testCasePass = dataQualityRepository.validateTaskRunTestCase(taskRunId);
            assertThat(testCasePass, is(DataQualityStatus.CREATED));
        }

        dataQualityRepository.updateCaseRunStatus(caseRunList.get(4).getCaseRunId(), false);
        testCasePass = dataQualityRepository.validateTaskRunTestCase(taskRunId);
        assertThat(testCasePass, is(DataQualityStatus.FAILED));
    }

    @Test
    public void updateCaseRunStatus() {
        //prepare
        List<Long> caseRunIdList = new ArrayList<>();
        caseRunIdList.add(IdGenerator.getInstance().nextId());
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        List<CaseRun> caseRunList = new ArrayList<>();
        for(int i = 0;i<caseRunIdList.size();i++){
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(caseRunIdList.get(i));
            caseRun.setTaskRunId(taskRunId);
            caseRun.setCaseId(IdGenerator.getInstance().nextId());
            caseRunList.add(caseRun);
        }
        dataQualityRepository.insertCaseRunWithTaskRun(caseRunList);
        Long caseRunId = caseRunIdList.get(0);

        //verify
        String caseRunStatus = dataQualityRepository.fetchCaseRunStatus(caseRunId);
        assertThat(caseRunStatus, is(DataQualityStatus.CREATED.name()));

        dataQualityRepository.updateCaseRunStatus(caseRunId, false);
        caseRunStatus = dataQualityRepository.fetchCaseRunStatus(caseRunId);
        assertThat(caseRunStatus, is(DataQualityStatus.FAILED.name()));

        dataQualityRepository.updateCaseRunStatus(caseRunId, true);
        caseRunStatus = dataQualityRepository.fetchCaseRunStatus(caseRunId);
        assertThat(caseRunStatus, is(DataQualityStatus.SUCCESS.name()));


    }


    @Test
    public void insertCaseRunWithTaskRun() {
        //prepare
        List<Long> caseRunIdList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            caseRunIdList.add(IdGenerator.getInstance().nextId());
        }
        Long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        List<CaseRun> caseRunList = new ArrayList<>();
        for(int i = 0;i<caseRunIdList.size();i++){
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(caseRunIdList.get(i));
            caseRun.setTaskRunId(taskRunId);
            caseRun.setCaseId(IdGenerator.getInstance().nextId());
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
