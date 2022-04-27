package com.miotech.kun.dataquality.facade;

import com.miotech.kun.dataquality.core.expectation.ValidationResult;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Expose expectation-run interface
 */
public interface ExpectationRunFacade {

    List<ValidationResult> fetchByUpdateTimeFromAndPassed(OffsetDateTime updateTimeFrom, boolean passed);

}
