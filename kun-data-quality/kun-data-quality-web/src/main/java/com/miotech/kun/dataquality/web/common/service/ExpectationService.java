package com.miotech.kun.dataquality.web.common.service;

import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.entity.CaseResult;
import com.miotech.kun.dataquality.web.model.entity.ExpectationBasic;
import com.miotech.kun.dataquality.core.model.ValidateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public ValidateResult validateTaskAttemptTestCase(Long taskAttemptId, List<CaseType> caseTypeList) {
        List<CaseResult> caseResultList = expectationDao.fetchValidateResult(taskAttemptId, caseTypeList);
        if (caseResultList.size() == 0) {
            return ValidateResult.SUCCESS;
        }
        int running = 0;
        int blockFailed = 0;
        int usingLatestFailed = 0;
        for (CaseResult caseResult : caseResultList) {
            DataQualityStatus dataQualityStatus = caseResult.getDataQualityStatus();
            if (dataQualityStatus.equals(DataQualityStatus.CREATED)) {
                running++;
                continue;
            }
            if (dataQualityStatus.equals(DataQualityStatus.FAILED)) {
                CaseType caseType = caseResult.getCaseType();
                if (caseType.equals(CaseType.BLOCK)) {
                    blockFailed++;
                }
                if (caseType.equals(CaseType.FINAL_SUCCESS)) {
                    usingLatestFailed++;
                }
            }
        }
        if (running > 0) {
            return ValidateResult.RUNNING;
        }
        if (blockFailed > 0) {
            return ValidateResult.BLOCK_CASE_FAILED;
        }
        if (usingLatestFailed > 0) {
            return ValidateResult.FINAL_SUCCESS_FAILED;
        }
        return ValidateResult.SUCCESS;
    }

    public ExpectationBasic fetchCaseBasicByTaskId(Long taskId) {
        return expectationDao.fetchCaseBasicByTaskId(taskId);
    }
}
