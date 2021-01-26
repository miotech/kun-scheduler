package com.miotech.kun.dataplatform.common.backfill.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillCreateInfo;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillSearchParams;
import com.miotech.kun.dataplatform.mocking.MockBackfillFactory;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class BackfillDaoTest extends AppTestBase {
    @Autowired
    private BackfillDao backfillDao;

    @Test
    public void createBackfill_withProperObject_shouldWork() {
        // 1. Prepare
        DateTimeUtils.freeze();
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

    @Test
    public void createBackfill_withProperInfoValueObject_shouldWork() {
        // 1. Prepare
        OffsetDateTime now = DateTimeUtils.freeze();
        BackfillCreateInfo createInfo = new BackfillCreateInfo(
                "backfill-created-by-info-vo",
                123L,
                // Task run ids
                Lists.newArrayList(101L, 102L, 103L),
                // Task def ids
                Lists.newArrayList(1L, 2L, 3L)
        );

        // 2. process
        Backfill backfillReturned = backfillDao.create(createInfo);

        // 3. Validate
        Optional<Backfill> persistedBackfill = backfillDao.fetchById(backfillReturned.getId());
        assertTrue(persistedBackfill.isPresent());
        assertThat(persistedBackfill.get(), sameBeanAs(backfillReturned));
        assertThat(backfillReturned.getName(), is(createInfo.getName()));
        assertThat(backfillReturned.getCreator(), is(createInfo.getCreator()));
        assertThat(backfillReturned.getTaskDefinitionIds(), is(createInfo.getTaskDefinitionIds()));
        assertThat(backfillReturned.getTaskRunIds(), is(createInfo.getTaskRunIds()));
        assertThat(backfillReturned.getCreateTime(), is(now));
        assertThat(backfillReturned.getUpdateTime(), is(now));

        // 4. tear down
        DateTimeUtils.resetClock();
    }

    @Test
    public void createBackfill_withDuplicatedId_shouldThrowException() {
        // 1. Prepare
        Backfill exampleBackfillInstance = MockBackfillFactory.createBackfill();

        // 2. Process
        // should work
        backfillDao.create(exampleBackfillInstance);

        try {
            // should throw exception
            backfillDao.create(exampleBackfillInstance);
            // 3. Validate
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(DuplicateKeyException.class));
        }
    }

    @Test
    public void findById_withNonExistingRecord_shouldReturnEmpty() {
        // 1. Prepare
        Backfill exampleBackfillInstance = MockBackfillFactory.createBackfill();
        backfillDao.create(exampleBackfillInstance);

        // 2. Process
        Optional<Backfill> persistedBackfill = backfillDao.fetchById(exampleBackfillInstance.getId());
        Optional<Backfill> nonExistingBackfill = backfillDao.fetchById(12345L);

        // 3. Validate
        assertTrue(persistedBackfill.isPresent());
        assertFalse(nonExistingBackfill.isPresent());
    }

    @Test
    public void search_withPagination_shouldWork() {
        // 1. Prepare
        List<Backfill> backfills = MockBackfillFactory.createBackfill(100);
        for (Backfill backfill : backfills) {
            backfillDao.create(backfill);
        }

        BackfillSearchParams searchParams1 = new BackfillSearchParams();
        searchParams1.setPageNumber(1);
        searchParams1.setPageSize(40);

        BackfillSearchParams searchParams2 = new BackfillSearchParams();
        searchParams2.setPageNumber(2);
        searchParams2.setPageSize(40);

        BackfillSearchParams searchParams3 = new BackfillSearchParams();
        searchParams3.setPageNumber(3);
        searchParams3.setPageSize(40);

        // 2. Process
        PageResult<Backfill> backfillPage1 = backfillDao.search(searchParams1);
        PageResult<Backfill> backfillPage2 = backfillDao.search(searchParams2);
        PageResult<Backfill> backfillPage3 = backfillDao.search(searchParams3);

        // 3. Validate
        // total count
        assertThat(backfillPage1.getTotalCount(), is(100));
        assertThat(backfillPage2.getTotalCount(), is(100));
        assertThat(backfillPage3.getTotalCount(), is(100));

        // page size
        assertThat(backfillPage1.getRecords().size(), is(40));
        assertThat(backfillPage2.getRecords().size(), is(40));
        assertThat(backfillPage3.getRecords().size(), is(20));

        // Pages should have no intersection when there's no modification
        Set<Long> page1Ids = backfillPage1.getRecords().stream().map(record -> record.getId()).collect(Collectors.toSet());
        Set<Long> page2Ids = backfillPage2.getRecords().stream().map(record -> record.getId()).collect(Collectors.toSet());
        Set<Long> page3Ids = backfillPage3.getRecords().stream().map(record -> record.getId()).collect(Collectors.toSet());

        assertTrue(Sets.intersection(page1Ids, page2Ids).isEmpty());
        assertTrue(Sets.intersection(page2Ids, page3Ids).isEmpty());
        assertTrue(Sets.intersection(page1Ids, page3Ids).isEmpty());
    }

    @Test
    public void search_withNameAsKeyword_shouldFilterProperly() {
        // 1. Prepare
        List<Backfill> backfills = MockBackfillFactory.createBackfill(100);
        for (Backfill backfill : backfills) {
            backfillDao.create(backfill);
        }
        BackfillSearchParams searchParams = new BackfillSearchParams();
        searchParams.setPageNumber(1);
        searchParams.setPageSize(100);
        searchParams.setName("example-backfill-1");

        // 2. Process
        PageResult<Backfill> resultPage = backfillDao.search(searchParams);

        // 3. validate
        // 1, 10, 11, ..., 19, 100
        assertThat(resultPage.getTotalCount(), is(12));
        assertThat(resultPage.getRecords().size(), is(12));
    }

    @Test
    public void search_withCreateTimeRange_shouldFilterProperly() {
        // 1. Prepare
        OffsetDateTime time1 = DateTimeUtils.freezeAt("202101251000");
        List<Backfill> backfillGroup1 = MockBackfillFactory.createBackfill(10, 0);

        OffsetDateTime time2 = DateTimeUtils.freezeAt("202101261000");
        List<Backfill> backfillGroup2 = MockBackfillFactory.createBackfill(10, 10);

        OffsetDateTime time3 = DateTimeUtils.freezeAt("202101261800");
        List<Backfill> backfillGroup3 = MockBackfillFactory.createBackfill(10, 20);

        for (Backfill backfill : backfillGroup1) {
            backfillDao.create(backfill);
        }
        for (Backfill backfill : backfillGroup2) {
            backfillDao.create(backfill);
        }
        for (Backfill backfill : backfillGroup3) {
            backfillDao.create(backfill);
        }

        // 2. Process
        BackfillSearchParams searchParams1 = new BackfillSearchParams();
        searchParams1.setPageNumber(1);
        searchParams1.setPageSize(100);
        searchParams1.setTimeRngStart(time1);
        searchParams1.setTimeRngEnd(time2.minus(1, ChronoUnit.SECONDS));

        BackfillSearchParams searchParams2 = new BackfillSearchParams();
        searchParams2.setPageNumber(1);
        searchParams2.setPageSize(100);
        searchParams2.setTimeRngStart(time1);
        searchParams2.setTimeRngEnd(time2);

        BackfillSearchParams searchParams3 = new BackfillSearchParams();
        searchParams3.setPageNumber(1);
        searchParams3.setPageSize(100);
        searchParams3.setTimeRngStart(time2);
        searchParams3.setTimeRngEnd(null);

        PageResult<Backfill> resultPage1 = backfillDao.search(searchParams1);
        PageResult<Backfill> resultPage2 = backfillDao.search(searchParams2);
        PageResult<Backfill> resultPage3 = backfillDao.search(searchParams3);

        // 3. Validate
        assertThat(resultPage1.getRecords().size(), is(10));
        assertThat(resultPage2.getRecords().size(), is(20));
        assertThat(resultPage3.getRecords().size(), is(20));

        Set<Long> page1Ids = resultPage1.getRecords().stream().map(Backfill::getId).collect(Collectors.toSet());
        Set<Long> page2Ids = resultPage2.getRecords().stream().map(Backfill::getId).collect(Collectors.toSet());
        Set<Long> page3Ids = resultPage3.getRecords().stream().map(Backfill::getId).collect(Collectors.toSet());

        Set<Long> group1Ids = backfillGroup1.stream().map(Backfill::getId).collect(Collectors.toSet());
        Set<Long> group2Ids = backfillGroup2.stream().map(Backfill::getId).collect(Collectors.toSet());
        Set<Long> group3Ids = backfillGroup3.stream().map(Backfill::getId).collect(Collectors.toSet());

        assertThat(page1Ids, is(group1Ids));
        assertThat(page2Ids, is(Sets.union(group1Ids, group2Ids)));
        assertThat(page3Ids, is(Sets.union(group2Ids, group3Ids)));

        // 4. Teardown
        DateTimeUtils.resetClock();
    }

    @Test
    public void search_filterByCreatorIds_shouldWork() {
        // 1. Prepare
        List<Backfill> backfills = MockBackfillFactory.createBackfill(50);
        for (Backfill backfill : backfills) {
            backfillDao.create(backfill);
        }
        BackfillSearchParams searchParams1 = new BackfillSearchParams();
        searchParams1.setPageNumber(1);
        searchParams1.setPageSize(100);
        searchParams1.setCreators(Lists.newArrayList(1L));

        BackfillSearchParams searchParams2 = new BackfillSearchParams();
        searchParams2.setPageNumber(1);
        searchParams2.setPageSize(100);
        searchParams2.setCreators(Lists.newArrayList(1L, 2L));

        BackfillSearchParams searchParams3 = new BackfillSearchParams();
        searchParams3.setPageNumber(1);
        searchParams3.setPageSize(100);
        searchParams3.setCreators(Lists.newArrayList(2L));

        // 2. Process
        PageResult<Backfill> resultPage1 = backfillDao.search(searchParams1);
        PageResult<Backfill> resultPage2 = backfillDao.search(searchParams2);
        PageResult<Backfill> resultPage3 = backfillDao.search(searchParams3);

        // 3. Validate
        assertThat(resultPage1.getRecords().size(), is(50));
        assertThat(resultPage2.getRecords().size(), is(50));
        assertThat(resultPage3.getRecords().size(), is(0));
    }
}
