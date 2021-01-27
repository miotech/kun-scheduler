package com.miotech.kun.metadata.databuilder.extract.schema;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.util.Iterator;
import java.util.List;

public interface DatasetSchemaExtractor extends DatasetExistenceExtractor {

    List<DatasetField> extract(Dataset dataset, DataSource dataSource);

    Iterator<Dataset> extract(DataSource dataSource);

}
