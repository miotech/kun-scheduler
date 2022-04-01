package com.miotech.kun.dataquality.web.common.service;

import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpectationService {

    @Autowired
    private ExpectationDao expectationDao;

    public Expectation fetchById(Long expectationId) {
        return expectationDao.fetchById(expectationId);
    }

    public Long getTaskId(Long expectationId) {
        Expectation expectation = fetchById(expectationId);
        if (expectation == null) {
            return null;
        }

        return expectation.getTaskId();
    }

    public void updateTaskId(Long expectationId, Long taskId) {
        expectationDao.updateTaskId(expectationId, taskId);
    }

    public Expectation fetchByTaskId(Long taskId) {
        return expectationDao.fetchByTaskId(taskId);
    }
}
