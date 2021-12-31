package com.miotech.kun.dataquality;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.ExpectationSpec;
import com.miotech.kun.dataquality.mock.MockExpectationSpecFactory;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.service.ExpectationService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        ExpectationSpec spec = MockExpectationSpecFactory.create();
        expectationDao.create(spec);

        Long taskId = expectationService.getTaskId(spec.getExpectationId());
        assertThat(taskId, is(spec.getTaskId()));
    }

}
