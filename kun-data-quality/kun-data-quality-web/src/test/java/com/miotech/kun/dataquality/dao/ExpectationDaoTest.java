package com.miotech.kun.dataquality.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.DataQualityTestBase;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.mock.MockDatasetBasicFactory;
import com.miotech.kun.dataquality.mock.MockExpectationFactory;
import com.miotech.kun.dataquality.mock.MockValidationResultFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.bo.ExpectationsRequest;
import com.miotech.kun.dataquality.web.model.entity.*;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doReturn;

public class ExpectationDaoTest extends DataQualityTestBase {

    @Autowired
    private ExpectationDao expectationDao;

    @Autowired
    private ExpectationRunDao expectationRunDao;

    @SpyBean
    private DatasetRepository datasetRepository;

    @Autowired
    private DataQualityRepository dataQualityRepository;

    @Test
    public void testCreateThenFetch() {
        Expectation expectation = MockExpectationFactory.create();
        expectationDao.create(expectation);

        Expectation fetched = expectationDao.fetchById(expectation.getExpectationId());
        assertThat(expectation, sameBeanAs(fetched).ignoring("dataset").ignoring("template").ignoring("payload"));
        assertThat(expectation.getDataset().getGid(), is(fetched.getDataset().getGid()));
        assertThat(expectation.getTemplate().getName(), is(fetched.getTemplate().getName()));
        assertThat(expectation.getPayload(), nullValue());
    }

    @Test
    public void testCreateThenFetch_withPayload() {
        Map<String, Object> payload = ImmutableMap.of("fields", ImmutableList.of("name", "age"));
        Expectation expectation = MockExpectationFactory.create(payload);
        expectationDao.create(expectation);

        Expectation fetched = expectationDao.fetchById(expectation.getExpectationId());
        assertThat(expectation, sameBeanAs(fetched).ignoring("dataset").ignoring("template").ignoring("payload"));
        assertThat(expectation.getDataset().getGid(), is(fetched.getDataset().getGid()));
        assertThat(expectation.getTemplate().getName(), is(fetched.getTemplate().getName()));
        Map<String, Object> payloadOfFetched = expectation.getPayload();
        assertThat((List<String>) payloadOfFetched.get("fields"), containsInAnyOrder("name", "age"));
    }

    @Test
    public void testDelete() {
        Expectation expectation = MockExpectationFactory.create();
        expectationDao.create(expectation);

        Expectation fetched = expectationDao.fetchById(expectation.getExpectationId());
        assertThat(fetched, notNullValue());

        expectationDao.deleteById(expectation.getExpectationId());
        fetched = expectationDao.fetchById(expectation.getExpectationId());
        assertThat(fetched, nullValue());
    }

    @Test
    public void testUpdate() {
        Expectation expectation = MockExpectationFactory.create();
        expectationDao.create(expectation);

        String newName = "new expectation name";
        Expectation newExpectation = expectation.cloneBuilder().withName(newName).build();
        expectationDao.updateById(expectation.getExpectationId(), newExpectation);

        Expectation fetched = expectationDao.fetchById(expectation.getExpectationId());
        assertThat(fetched, notNullValue());
        assertThat(fetched.getName(), is(newName));
    }

    @Test
    public void testGetHistoryOfTheLastNTimes_illegalArgs() {
        List<Long> expectationIds = Lists.newArrayList();
        int n = 1;
        try {
            expectationDao.getHistoryOfTheLastNTimes(expectationIds, n);
        } catch (Throwable t) {
            Assert.assertThat(t.getClass(), is(IllegalArgumentException.class));
        }

        expectationIds = Lists.newArrayList(1L);
        n = 0;
        try {
            expectationDao.getHistoryOfTheLastNTimes(expectationIds, n);
        } catch (Throwable t) {
            Assert.assertThat(t.getClass(), is(IllegalArgumentException.class));
        }
    }

    @Test
    public void testGetHistoryOfTheLastNTimes_success() {
        ValidationResult vr = MockValidationResultFactory.create();
        expectationRunDao.create(vr);

        List<Long> expectationIds = ImmutableList.of(vr.getExpectationId());
        int n = 1;
        List<DataQualityHistoryRecords> historyOfTheLastNTimes = expectationDao.getHistoryOfTheLastNTimes(expectationIds, n);
        assertThat(historyOfTheLastNTimes.size(), is(1));
    }

    @Test
    public void testGetRelatedDatasets() {
        // prepare
        Expectation spec = MockExpectationFactory.create();
        expectationDao.create(spec);
        expectationDao.createRelatedDataset(spec.getExpectationId(), ImmutableList.of(spec.getDataset().getGid()));

        // mock datasetRepository
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create();
        doReturn(datasetBasic).when(datasetRepository).findBasic(spec.getDataset().getGid());

        List<DatasetBasic> relatedDatasets = expectationDao.getRelatedDatasets(spec.getExpectationId());
        assertThat(relatedDatasets.size(), is(1));
    }

    @Test
    public void testGetExpectationBasic() {
        Expectation spec = MockExpectationFactory.create();
        expectationDao.create(spec);
        expectationDao.createRelatedDataset(spec.getExpectationId(), ImmutableList.of(spec.getDataset().getGid()));

        ExpectationsRequest expectationsRequest = new ExpectationsRequest();
        expectationsRequest.setGid(spec.getDataset().getGid());
        ExpectationBasics expectationBasics = expectationDao.getExpectationBasics(expectationsRequest);
        assertThat(expectationBasics.getTotalCount(), is(1));
        assertThat(expectationBasics.getExpectationBasics().size(), is(1));
        ExpectationBasic expectationBasic = expectationBasics.getExpectationBasics().get(0);
        assertThat(expectationBasic.getId(), is(spec.getExpectationId()));
        assertThat(expectationBasic.getName(), is(spec.getName()));
        assertThat(expectationBasic.getUpdater(), is(spec.getUpdateUser()));
        assertThat(expectationBasic.getTaskId(), is(spec.getTaskId()));
    }

    @Test
    public void testFetchByTaskId_empty() {
        Long taskId = IdGenerator.getInstance().nextId();
        Expectation expectation = expectationDao.fetchByTaskId(taskId);
        assertThat(expectation, nullValue());
    }

    @Test
    public void testFetchByTaskId() {
        Expectation expectation = MockExpectationFactory.create();
        expectationDao.create(expectation);

        Expectation fetched = expectationDao.fetchByTaskId(expectation.getTaskId());
        assertThat(fetched, sameBeanAs(expectation).ignoring("dataset").ignoring("template").ignoring("payload"));
        assertThat(fetched.getDataset().getGid(), is(expectation.getDataset().getGid()));
        assertThat(expectation.getTemplate().getName(), is(fetched.getTemplate().getName()));
        assertThat(expectation.getPayload(), nullValue());
    }

    @Test
    public void fetchValidateResult_should_only_contains_specified_type() {
        //prepare
        Long taskRunId = 1l;
        Long taskAttemptId = 2l;
        List<CaseRun> caseRunList = new ArrayList<>();
        //prepare three case
        //1:type:block 2: type:using_latest 3:skip
        Expectation expectation1 = MockExpectationFactory.create()
                .cloneBuilder()
                .withCaseType(CaseType.BLOCK)
                .build();
        Expectation expectation2 = MockExpectationFactory.create()
                .cloneBuilder()
                .withCaseType(CaseType.FINAL_SUCCESS)
                .build();
        Expectation expectation3 = MockExpectationFactory.create()
                .cloneBuilder()
                .withCaseType(CaseType.SKIP)
                .build();
        List<Expectation> expectationList = Lists.newArrayList(expectation1, expectation2, expectation3);
        for (Expectation expectation : expectationList) {
            expectationDao.create(expectation);
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseRunId(IdGenerator.getInstance().nextId());
            caseRun.setTaskRunId(taskRunId);
            caseRun.setTaskAttemptId(taskAttemptId);
            caseRun.setValidateVersion("V1");
            caseRun.setCaseId(expectation.getExpectationId());
            caseRun.setValidateDatasetId(expectation.getDataset().getGid());
            caseRun.setStatus(DataQualityStatus.SUCCESS);
            caseRunList.add(caseRun);
        }


        dataQualityRepository.insertCaseRunWithTaskRun(caseRunList);

        List<CaseResult> caseResultList = expectationDao.fetchValidateResult(taskAttemptId, org.assertj.core.util.Lists.newArrayList(CaseType.BLOCK, CaseType.FINAL_SUCCESS));

        //verify
        assertThat(caseResultList.size(), is(2));
        List<CaseType> caseTypeList = caseResultList.stream().map(CaseResult::getCaseType).collect(Collectors.toList());
        assertThat(caseTypeList, containsInAnyOrder(CaseType.BLOCK, CaseType.FINAL_SUCCESS));

    }

}
