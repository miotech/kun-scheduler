package com.miotech.kun.dataquality;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.mock.MockExpectationFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.service.ExpectationService;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.entity.CaseRun;
import com.miotech.kun.dataquality.core.model.ValidateResult;
import com.miotech.kun.dataquality.web.service.DataQualityService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ExpectationServiceTest extends DataQualityTestBase {

    @Autowired
    private ExpectationService expectationService;

    @Autowired
    private ExpectationDao expectationDao;

    @Autowired
    private DataQualityService dataQualityService;

    @Test
    public void testGetTaskId_nonExist() {
        Long expectationId = IdGenerator.getInstance().nextId();
        Long taskId = expectationService.getTaskId(expectationId);
        assertThat(taskId, nullValue());
    }

    @Test
    public void testGetTaskId() {
        // prepare
        Expectation spec = MockExpectationFactory.create();
        expectationDao.create(spec);

        Long taskId = expectationService.getTaskId(spec.getExpectationId());
        assertThat(taskId, is(spec.getTaskId()));
    }

    @Test
    public void testFetchByTaskId_empty() {
        Long taskId = IdGenerator.getInstance().nextId();
        Expectation expectation = expectationService.fetchByTaskId(taskId);
        assertThat(expectation, nullValue());
    }

    @Test
    public void testFetchByTaskId() {
        Expectation expectation = MockExpectationFactory.create();
        expectationDao.create(expectation);

        Expectation fetched = expectationService.fetchByTaskId(expectation.getTaskId());
        assertThat(fetched, sameBeanAs(expectation).ignoring("dataset").ignoring("metrics").ignoring("assertion"));
        assertThat(fetched.getDataset().getGid(), is(expectation.getDataset().getGid()));
        assertThat(fetched.getMetrics().getName(), is(expectation.getMetrics().getName()));
        assertThat(fetched.getAssertion().getExpectedValue(), is(expectation.getAssertion().getExpectedValue()));
    }

    @ParameterizedTest
    @MethodSource("caseRunParams")
    public void validateTaskAttemptTestCase(List<DataQualityStatus> dataQualityStatuses, List<CaseType> caseTypes, ValidateResult expected) {
        //prepare
        Long taskRunId = 1l;
        Long taskAttemptId = 2l;
        List<CaseRun> caseRunList = new ArrayList<>();
        for (int i = 0; i < dataQualityStatuses.size(); i++) {
            DataQualityStatus dataQualityStatus = dataQualityStatuses.get(i);
            Expectation expectation = MockExpectationFactory.create()
                    .cloneBuilder()
                    .withCaseType(caseTypes.get(i))
                    .build();
            expectationDao.create(expectation);
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(IdGenerator.getInstance().nextId());
            caseRun.setTaskRunId(taskRunId);
            caseRun.setTaskAttemptId(taskAttemptId);
            caseRun.setValidateVersion("V1");
            caseRun.setCaseId(expectation.getExpectationId());
            caseRun.setValidateDatasetId(expectation.getDataset().getGid());
            caseRun.setStatus(dataQualityStatus);
            caseRunList.add(caseRun);
        }
        dataQualityService.insertCaseRunWithTaskRun(caseRunList);
        for (CaseRun caseRun : caseRunList){
            dataQualityService.updateCaseRunStatus(caseRun);
        }

        ValidateResult validateResult = expectationService.validateTaskAttemptTestCase(taskAttemptId, caseTypes);

        //verify
        assertThat(validateResult, is(expected));

    }

    @Test
    public void validateTaskAttemptTestCase_should_not_contains_side_dataset() {
        //prepare
        Long taskRunId = 1l;
        Long taskAttemptId = 2l;
        Long datasetId = IdGenerator.getInstance().nextId();
        List<CaseRun> caseRunList = new ArrayList<>();
        //main case all success
        for (int i = 0; i < 3; i++) {
            Expectation expectation = MockExpectationFactory.create(datasetId)
                    .cloneBuilder()
                    .withCaseType(CaseType.BLOCK)
                    .build();
            expectationDao.create(expectation);
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(IdGenerator.getInstance().nextId());
            caseRun.setTaskRunId(taskRunId);
            caseRun.setTaskAttemptId(taskAttemptId);
            caseRun.setValidateVersion("V1");
            caseRun.setCaseId(expectation.getExpectationId());
            caseRun.setValidateDatasetId(datasetId);
            caseRun.setStatus(DataQualityStatus.SUCCESS);
            caseRunList.add(caseRun);
        }
        //side case failed
        Expectation expectation = MockExpectationFactory.create()
                .cloneBuilder()
                .withCaseType(CaseType.BLOCK)
                .build();
        expectationDao.create(expectation);
        CaseRun failedCaseRun = new CaseRun();
        failedCaseRun.setCaseRunId(IdGenerator.getInstance().nextId());
        failedCaseRun.setTaskRunId(taskRunId);
        failedCaseRun.setTaskAttemptId(taskAttemptId);
        failedCaseRun.setValidateVersion("V1");
        failedCaseRun.setCaseId(expectation.getExpectationId());
        failedCaseRun.setValidateDatasetId(datasetId);
        failedCaseRun.setStatus(DataQualityStatus.FAILED);
        caseRunList.add(failedCaseRun);

        dataQualityService.insertCaseRunWithTaskRun(caseRunList);


        for (CaseRun caseRun : caseRunList){
            dataQualityService.updateCaseRunStatus(caseRun);
        }

        ValidateResult validateResult = expectationService.validateTaskAttemptTestCase(taskAttemptId,Lists.newArrayList(CaseType.BLOCK,CaseType.FINAL_SUCCESS));

        //verify
        assertThat(validateResult, is(ValidateResult.SUCCESS));

    }

    public static Stream<Arguments> caseRunParams() {
        List<CaseType> caseTypes = Lists.newArrayList(CaseType.BLOCK,CaseType.FINAL_SUCCESS);
        return Stream.of(
                Arguments.of(Lists.newArrayList(DataQualityStatus.FAILED,DataQualityStatus.FAILED),
                        caseTypes,ValidateResult.BLOCK_CASE_FAILED),
                Arguments.of(Lists.newArrayList(DataQualityStatus.FAILED,DataQualityStatus.CREATED),
                        caseTypes,ValidateResult.RUNNING),
                Arguments.of(Lists.newArrayList(DataQualityStatus.FAILED,DataQualityStatus.SUCCESS),
                        caseTypes,ValidateResult.BLOCK_CASE_FAILED),
                Arguments.of(Lists.newArrayList(DataQualityStatus.CREATED,DataQualityStatus.FAILED),
                        caseTypes,ValidateResult.RUNNING),
                Arguments.of(Lists.newArrayList(DataQualityStatus.CREATED,DataQualityStatus.CREATED),
                        caseTypes,ValidateResult.RUNNING),
                Arguments.of(Lists.newArrayList(DataQualityStatus.CREATED,DataQualityStatus.SUCCESS),
                        caseTypes,ValidateResult.RUNNING),
                Arguments.of(Lists.newArrayList(DataQualityStatus.SUCCESS,DataQualityStatus.FAILED),
                        caseTypes,ValidateResult.FINAL_SUCCESS_FAILED),
                Arguments.of(Lists.newArrayList(DataQualityStatus.SUCCESS,DataQualityStatus.CREATED),
                        caseTypes,ValidateResult.RUNNING),
                Arguments.of(Lists.newArrayList(DataQualityStatus.SUCCESS,DataQualityStatus.SUCCESS),
                        caseTypes,ValidateResult.SUCCESS)
                );

    }

}
