package com.miotech.kun.dataquality;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.mock.MockExpectationFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.service.ExpectationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ExpectationServiceTest extends DataQualityTestBase {

    @Autowired
    private ExpectationService expectationService;

    @Autowired
    private ExpectationDao expectationDao;

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

}
