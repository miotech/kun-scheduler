package com.miotech.kun.metadata.extract.factory;

import com.miotech.kun.metadata.extract.DatasetExtractor;
import com.miotech.kun.metadata.model.Database;

public interface DatasetExtractorFactory<T> {

    DatasetExtractor newInstance(T name, Database database);

}
