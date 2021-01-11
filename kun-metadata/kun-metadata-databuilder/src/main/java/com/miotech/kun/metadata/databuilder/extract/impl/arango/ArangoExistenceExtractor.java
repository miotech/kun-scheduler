package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.ArangoClient;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;

public class ArangoExistenceExtractor implements DatasetExistenceExtractor {

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        ArangoClient arangoClient = null;
        try {
            arangoClient = new ArangoClient((ArangoDataSource) dataSource);
            return arangoClient.judgeTableExistence(dataset.getDatabaseName(), dataset.getName());
        } finally {
            if (arangoClient != null) {
                arangoClient.close();
            }
        }
    }

}
