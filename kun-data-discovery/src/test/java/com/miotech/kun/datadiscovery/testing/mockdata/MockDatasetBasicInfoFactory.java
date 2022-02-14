package com.miotech.kun.datadiscovery.testing.mockdata;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.vo.DatasetBasicInfo;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

public class MockDatasetBasicInfoFactory {

    private MockDatasetBasicInfoFactory() {
    }

    public static DatasetBasicInfo create() {
        DatasetBasicInfo datasetBasicInfo = new DatasetBasicInfo();
        datasetBasicInfo.setGid(IdGenerator.getInstance().nextId());
        datasetBasicInfo.setName("test_table");
        datasetBasicInfo.setDatasource("hive");
        datasetBasicInfo.setDatabase("default");
        datasetBasicInfo.setSchema("public");
        datasetBasicInfo.setDescription("desc");
        datasetBasicInfo.setType("hive");
        datasetBasicInfo.setHighWatermark(null);
        datasetBasicInfo.setLowWatermark(null);
        datasetBasicInfo.setOwners(ImmutableList.of("admin"));
        datasetBasicInfo.setTags(ImmutableList.of("tag1"));
        datasetBasicInfo.setDeleted(false);
        return datasetBasicInfo;
    }

}
