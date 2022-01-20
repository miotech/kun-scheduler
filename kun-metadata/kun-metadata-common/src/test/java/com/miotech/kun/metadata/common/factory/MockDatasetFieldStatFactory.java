package com.miotech.kun.metadata.common.factory;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;

public class MockDatasetFieldStatFactory {

    private MockDatasetFieldStatFactory() {
    }

    public static FieldStatistics create() {
        return FieldStatistics.newBuilder()
                .withFieldName("test_field")
                .withNonnullCount(100L)
                .withDistinctCount(100L)
                .withUpdatedBy("admin")
                .withStatDate(DateTimeUtils.now())
                .build();
    }

}
