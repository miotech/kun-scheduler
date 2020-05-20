package com.miotech.kun.metadata.extract.factory;

import com.miotech.kun.metadata.extract.DatasetExtractor;
import com.miotech.kun.metadata.extract.impl.HiveTableExtractor;
import com.miotech.kun.metadata.model.Database;

public class HiveDatasetExtractorFactory implements DatasetExtractorFactory {

    @Override
    public DatasetExtractor newInstance(Object name, Database database) {
        String table = (String) name;
        return new HiveTableExtractor(database, table);
    }

}
