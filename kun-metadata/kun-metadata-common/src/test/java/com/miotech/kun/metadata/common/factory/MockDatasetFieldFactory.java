package com.miotech.kun.metadata.common.factory;

import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;

public class MockDatasetFieldFactory {

    private MockDatasetFieldFactory() {
    }

    public static DatasetField create() {
        return DatasetField.newBuilder()
                .withName("test_field")
                .withFieldType(new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "varchar"))
                .withComment("comment")
                .withIsPrimaryKey(false)
                .withIsNullable(true)
                .build();
    }

}
