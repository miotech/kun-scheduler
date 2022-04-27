package com.miotech.kun.dataquality.web.common.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.facade.ExpectationRunFacade;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ExpectationRunService implements ExpectationRunFacade {

    @Autowired
    private ExpectationRunDao expectationRunDao;

    @Override
    public List<ValidationResult> fetchByUpdateTimeFromAndPassed(OffsetDateTime updateTimeFrom, boolean passed) {
        Preconditions.checkNotNull(updateTimeFrom, "Argument `updateTimeFrom` should not be null.");
        return expectationRunDao.fetchByUpdateTimeFromAndPassed(updateTimeFrom, passed);
    }
}
