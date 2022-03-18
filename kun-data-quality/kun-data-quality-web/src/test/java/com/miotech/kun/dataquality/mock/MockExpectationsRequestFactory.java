package com.miotech.kun.dataquality.mock;

import com.miotech.kun.dataquality.web.model.bo.ExpectationsRequest;

public class MockExpectationsRequestFactory {

    private MockExpectationsRequestFactory() {
    }

    public static ExpectationsRequest create(Long datasetGid) {
        ExpectationsRequest expectationsRequest = new ExpectationsRequest();
        expectationsRequest.setGid(datasetGid);

        return expectationsRequest;
    }
}
