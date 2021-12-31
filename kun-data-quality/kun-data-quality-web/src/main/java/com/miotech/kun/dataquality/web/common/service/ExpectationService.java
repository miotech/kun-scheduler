package com.miotech.kun.dataquality.web.common.service;

import com.miotech.kun.dataquality.core.ExpectationSpec;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpectationService {

    @Autowired
    private ExpectationDao expectationDao;

    public ExpectationSpec fetchById(Long expectationId) {
        return expectationDao.fetchById(expectationId);
    }

    public Long getTaskId(Long expectationId) {
        ExpectationSpec expectationSpec = fetchById(expectationId);
        if (expectationSpec == null) {
            return null;
        }

        return expectationSpec.getTaskId();
    }

    public void updateTaskId(Long expectationId, Long taskId) {
        expectationDao.updateTaskId(expectationId, taskId);
    }
}
