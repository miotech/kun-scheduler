package com.miotech.kun.dataplatform.common.backfill.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.mocking.MockBackfillFactory;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Optional;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertTrue;

public class BackfillDaoTest extends AppTestBase {
    @Autowired
    private BackfillDao backfillDao;

    @Test
    public void createBackfill_withProperObject_shouldWork() {
        // 1. Prepare
        OffsetDateTime now = DateTimeUtils.freeze();
        Backfill exampleBackfillInstance = MockBackfillFactory.createBackfill();

        // 2. Process
        Backfill returnedBackfill = backfillDao.create(exampleBackfillInstance);

        // 3. Validate
        Optional<Backfill> persistedBackfill = backfillDao.fetchById(exampleBackfillInstance.getId());
        assertTrue(persistedBackfill.isPresent());
        assertThat(persistedBackfill.get(), sameBeanAs(exampleBackfillInstance));
        assertThat(returnedBackfill, sameBeanAs(exampleBackfillInstance));

        // 4. tear down
        DateTimeUtils.resetClock();
    }
}
