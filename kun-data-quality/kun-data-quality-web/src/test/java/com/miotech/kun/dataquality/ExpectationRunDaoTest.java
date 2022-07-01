package com.miotech.kun.dataquality;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.mock.MockValidationResultFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpectationRunDaoTest extends DataQualityTestBase {

    @Autowired
    private ExpectationRunDao expectationRunDao;

    @Test
    public void testCreateThenFetch() {
        ValidationResult validationResult = MockValidationResultFactory.create();
        expectationRunDao.create(validationResult);

        ValidationResult fetched = expectationRunDao.fetchByExpectationId(validationResult.getExpectationId());
        assertThat(fetched, sameBeanAs(validationResult).ignoring("assertionResults").ignoring("continuousFailingCount"));
        assertThat(fetched.getContinuousFailingCount(), is(1L));
        assertThat(fetched.getAssertionResults().size(), is(1));
        assertThat(validationResult.getAssertionResults().get(0), sameBeanAs(fetched.getAssertionResults().get(0)));
    }

    @Test
    public void testGetLatestFailingCount_empty() {
        Long expectationId = IdGenerator.getInstance().nextId();
        long latestFailingCount = expectationRunDao.getLatestFailingCount(expectationId);
        assertThat(latestFailingCount, is(0L));
    }

    @Test
    public void testGetLatestFailingCount() {
        ValidationResult validationResult1 = MockValidationResultFactory.create();
        expectationRunDao.create(validationResult1);
        ValidationResult validationResult2 = MockValidationResultFactory.create(validationResult1.getExpectationId());
        expectationRunDao.create(validationResult2);

        long latestFailingCount = expectationRunDao.getLatestFailingCount(validationResult1.getExpectationId());
        assertThat(latestFailingCount, is(2L));
    }

    @Test
    public void testDelete() {
        ValidationResult validationResult = MockValidationResultFactory.create();
        expectationRunDao.create(validationResult);

        ValidationResult fetched = expectationRunDao.fetchByExpectationId(validationResult.getExpectationId());
        assertThat(fetched, notNullValue());

        expectationRunDao.deleteByExpectationId(fetched.getExpectationId());
        assertThrows(EmptyResultDataAccessException.class, () -> expectationRunDao.fetchByExpectationId(validationResult.getExpectationId()));

    }

}
