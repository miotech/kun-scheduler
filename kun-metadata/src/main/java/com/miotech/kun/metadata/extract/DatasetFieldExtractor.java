package com.miotech.kun.metadata.extract;

import com.miotech.kun.metadata.model.DatasetField;

import java.util.List;

public interface DatasetFieldExtractor {

    List<DatasetField> extract();
}
