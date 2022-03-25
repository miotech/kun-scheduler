package com.miotech.kun.dataquality;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.mock.MockDatasetBasicFactory;
import com.miotech.kun.dataquality.mock.MockExpectationSpecFactory;
import com.miotech.kun.dataquality.mock.MockValidationResultFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.model.bo.ExpectationsRequest;
import com.miotech.kun.dataquality.web.model.entity.DataQualityHistoryRecords;
import com.miotech.kun.dataquality.web.model.entity.DatasetBasic;
import com.miotech.kun.dataquality.web.model.entity.ExpectationBasic;
import com.miotech.kun.dataquality.web.model.entity.ExpectationBasics;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

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

    @Test
    public void testCreateThenFetch() {
        Expectation expectation = MockExpectationSpecFactory.create();
        expectationDao.create(expectation);

        Expectation fetched = expectationDao.fetchById(expectation.getExpectationId());
        assertThat(expectation, sameBeanAs(fetched).ignoring("dataset").ignoring("metrics").ignoring("assertion"));
        assertThat(expectation.getDataset().getGid(), is(fetched.getDataset().getGid()));
        assertThat(expectation.getMetrics().getName(), is(fetched.getMetrics().getName()));
        assertThat(expectation.getAssertion().getExpectedValue(), is(fetched.getAssertion().getExpectedValue()));
    }

    @Test
    public void testDelete() {
        Expectation expectation = MockExpectationSpecFactory.create();
        expectationDao.create(expectation);

        Expectation fetched = expectationDao.fetchById(expectation.getExpectationId());
        assertThat(fetched, notNullValue());

        expectationDao.deleteById(expectation.getExpectationId());
        fetched = expectationDao.fetchById(expectation.getExpectationId());
        assertThat(fetched, nullValue());
    }

    @Test
    public void testUpdate() {
        Expectation expectation = MockExpectationSpecFactory.create();
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
        Expectation spec = MockExpectationSpecFactory.create();
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
        Expectation spec = MockExpectationSpecFactory.create();
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

}
