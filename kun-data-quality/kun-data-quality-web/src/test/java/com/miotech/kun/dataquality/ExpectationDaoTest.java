package com.miotech.kun.dataquality;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.dataquality.core.ExpectationSpec;
import com.miotech.kun.dataquality.core.ValidationResult;
import com.miotech.kun.dataquality.mock.MockDatasetBasicFactory;
import com.miotech.kun.dataquality.mock.MockExpectationSpecFactory;
import com.miotech.kun.dataquality.mock.MockValidationResultFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.model.bo.DataQualitiesRequest;
import com.miotech.kun.dataquality.web.model.entity.DataQualityCaseBasic;
import com.miotech.kun.dataquality.web.model.entity.DataQualityCaseBasics;
import com.miotech.kun.dataquality.web.model.entity.DataQualityHistoryRecords;
import com.miotech.kun.dataquality.web.model.entity.DatasetBasic;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import org.junit.Assert;
import org.junit.Test;
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
        ExpectationSpec expectationSpec = MockExpectationSpecFactory.create();
        expectationDao.create(expectationSpec);

        ExpectationSpec fetched = expectationDao.fetchById(expectationSpec.getExpectationId());
        assertThat(expectationSpec, sameBeanAs(fetched).ignoring("dataset"));
        assertThat(expectationSpec.getDataset().getGid(), is(fetched.getDataset().getGid()));
    }

    @Test
    public void testDelete() {
        ExpectationSpec expectationSpec = MockExpectationSpecFactory.create();
        expectationDao.create(expectationSpec);

        ExpectationSpec fetched = expectationDao.fetchById(expectationSpec.getExpectationId());
        assertThat(fetched, notNullValue());

        expectationDao.deleteById(expectationSpec.getExpectationId());
        fetched = expectationDao.fetchById(expectationSpec.getExpectationId());
        assertThat(fetched, nullValue());
    }

    @Test
    public void testUpdate() {
        ExpectationSpec expectationSpec = MockExpectationSpecFactory.create();
        expectationDao.create(expectationSpec);

        String newName = "new expectation name";
        ExpectationSpec newExpectationSpec = expectationSpec.cloneBuilder().withName(newName).build();
        expectationDao.updateById(expectationSpec.getExpectationId(), newExpectationSpec);

        ExpectationSpec fetched = expectationDao.fetchById(expectationSpec.getExpectationId());
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
        ExpectationSpec spec = MockExpectationSpecFactory.create();
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
        ExpectationSpec spec = MockExpectationSpecFactory.create();
        expectationDao.create(spec);
        expectationDao.createRelatedDataset(spec.getExpectationId(), ImmutableList.of(spec.getDataset().getGid()));

        DataQualitiesRequest dataQualitiesRequest = new DataQualitiesRequest();
        dataQualitiesRequest.setGid(spec.getDataset().getGid());
        DataQualityCaseBasics expectationBasic = expectationDao.getExpectationBasic(dataQualitiesRequest);
        assertThat(expectationBasic.getTotalCount(), is(1));
        assertThat(expectationBasic.getDqCases().size(), is(1));
        DataQualityCaseBasic dataQualityCaseBasic = expectationBasic.getDqCases().get(0);
        assertThat(dataQualityCaseBasic.getId(), is(spec.getExpectationId()));
        assertThat(dataQualityCaseBasic.getName(), is(spec.getName()));
        assertThat(dataQualityCaseBasic.getUpdater(), is(spec.getUpdateUser()));
        assertThat(dataQualityCaseBasic.getTaskId(), is(spec.getTaskId()));
    }

}
