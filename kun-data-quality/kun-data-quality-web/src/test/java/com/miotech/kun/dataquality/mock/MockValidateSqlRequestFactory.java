package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.web.model.bo.ValidateSqlRequest;
import com.miotech.kun.dataquality.web.model.entity.DataQualityRule;

import java.util.List;

public class MockValidateSqlRequestFactory {

    private MockValidateSqlRequestFactory() {
    }

    public static ValidateSqlRequest create(String sqlText, List<DataQualityRule> rules) {
        Long datasetId = IdGenerator.getInstance().nextId();

        return create(datasetId, sqlText, rules);
    }

    public static ValidateSqlRequest create(Long datasetId, String sqlText, List<DataQualityRule> rules) {
        ValidateSqlRequest vsr  = new ValidateSqlRequest();
        vsr.setDatasetId(datasetId);
        vsr.setSqlText(sqlText);
        vsr.setValidateRules(rules);

        return vsr;
    }

}
