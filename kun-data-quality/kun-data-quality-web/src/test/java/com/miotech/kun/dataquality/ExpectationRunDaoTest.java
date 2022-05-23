package com.miotech.kun.dataquality;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.AssertionResult;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.mock.MockValidationResultFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.OffsetDateTime;
import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    public void testFetchByUpdateTimeFromAndPassed_empty() {
        OffsetDateTime now = DateTimeUtils.now();
        List<ValidationResult> validationResults = expectationRunDao.fetchByUpdateTimeFromAndPassed(now, false);
        assertThat(validationResults, empty());

        ValidationResult validationResult = MockValidationResultFactory.create(false);
        expectationRunDao.create(validationResult);

        OffsetDateTime tomorrow = now.plusDays(1);
        validationResults = expectationRunDao.fetchByUpdateTimeFromAndPassed(tomorrow, false);
        assertThat(validationResults, empty());

        OffsetDateTime yesterday = now.minusDays(1);
        validationResults = expectationRunDao.fetchByUpdateTimeFromAndPassed(yesterday, true);
        assertThat(validationResults, empty());
    }

    @Test
    public void testFetchByUpdateTimeFromAndPassed_shouldReturnData() {
        OffsetDateTime now = DateTimeUtils.now();
        Long expectationId = IdGenerator.getInstance().nextId();
        ValidationResult validationResult1 = MockValidationResultFactory.create(expectationId,false, now.minusDays(1));
        ValidationResult validationResult2 = MockValidationResultFactory.create(expectationId,false, now.plusDays(1));
        expectationRunDao.create(validationResult1);
        expectationRunDao.create(validationResult2);

        OffsetDateTime today = DateTimeUtils.now();
        List<ValidationResult> validationResults = expectationRunDao.fetchByUpdateTimeFromAndPassed(today, false);
        assertThat(validationResults.size(), is(1));
        ValidationResult validationResultOfFetched = validationResults.get(0);
        assertThat(validationResultOfFetched.getExpectationId(), is(validationResult2.getExpectationId()));
        assertThat(validationResultOfFetched.getContinuousFailingCount(), is(2L));
        assertThat(validationResultOfFetched.getUpdateTime(), is(validationResult2.getUpdateTime()));

        OffsetDateTime theDayBeforeYesterday = today.minusDays(2);
        validationResults = expectationRunDao.fetchByUpdateTimeFromAndPassed(theDayBeforeYesterday, false);
        assertThat(validationResults.size(), is(2));
    }


    @Test
    public void testDelete() {
        ValidationResult validationResult = MockValidationResultFactory.create();
        expectationRunDao.create(validationResult);

        ValidationResult fetched = expectationRunDao.fetchByExpectationId(validationResult.getExpectationId());
        assertThat(fetched, notNullValue());

        expectationRunDao.deleteByExpectationId(fetched.getExpectationId());
        try {
            expectationRunDao.fetchByExpectationId(validationResult.getExpectationId());
        } catch (Exception e) {
            assertThat(e, instanceOf(EmptyResultDataAccessException.class));
        }
    }

}
