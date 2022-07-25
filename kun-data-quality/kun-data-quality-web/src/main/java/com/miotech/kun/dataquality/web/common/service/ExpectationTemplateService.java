package com.miotech.kun.dataquality.web.common.service;

import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.dataquality.web.common.dao.ExpectationTemplateDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpectationTemplateService {

    @Autowired
    private ExpectationTemplateDao expectationTemplateDao;

    public void create(ExpectationTemplate expression) {
        expectationTemplateDao.create(expression);
    }

    public ExpectationTemplate fetchByName(String name) {
        return expectationTemplateDao.fetchByName(name);
    }

    public List<ExpectationTemplate> fetchByGranularity(String granularity) {
        return expectationTemplateDao.fetchByGranularity(granularity);
    }

}
