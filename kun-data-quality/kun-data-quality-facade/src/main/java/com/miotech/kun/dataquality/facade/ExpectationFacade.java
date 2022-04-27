package com.miotech.kun.dataquality.facade;

import com.miotech.kun.dataquality.core.expectation.Expectation;

/**
 * Expose expectation interface
 */
public interface ExpectationFacade {

    Expectation fetchById(Long expectationId);

}
