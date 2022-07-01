package com.miotech.kun.dataquality.web.common.service;

import com.miotech.kun.dataquality.facade.ExpectationRunFacade;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpectationRunService implements ExpectationRunFacade {

    @Autowired
    private ExpectationRunDao expectationRunDao;

}
