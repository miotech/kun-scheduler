package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.web.model.entity.DatasetBasic;

public class MockDatasetBasicFactory {

    private MockDatasetBasicFactory() {
    }

    public static DatasetBasic create() {
        DatasetBasic datasetBasic = new DatasetBasic();
        datasetBasic.setGid(IdGenerator.getInstance().nextId());
        datasetBasic.setName("dataset");
        datasetBasic.setDatabase("test");
        datasetBasic.setDatasource("hive");
        datasetBasic.setDatasourceType("Hive");
        datasetBasic.setIsPrimary(true);
        return datasetBasic;
    }

    public static DatasetBasic create(String database, String name, String type) {
        DatasetBasic datasetBasic = new DatasetBasic();
        datasetBasic.setGid(IdGenerator.getInstance().nextId());
        datasetBasic.setName(name);
        datasetBasic.setDatabase(database);
        datasetBasic.setDatasource("hive");
        datasetBasic.setDatasourceType(type);
        datasetBasic.setIsPrimary(true);
        return datasetBasic;
    }

}
