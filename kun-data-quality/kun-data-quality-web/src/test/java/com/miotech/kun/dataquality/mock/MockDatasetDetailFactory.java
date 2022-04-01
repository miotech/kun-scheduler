package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.web.model.entity.DatasetBasic;
import com.miotech.kun.metadata.core.model.vo.DatasetDetail;
import com.miotech.kun.metadata.core.model.vo.Watermark;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

public class MockDatasetDetailFactory {

    private MockDatasetDetailFactory() {
    }

    public static DatasetDetail create() {
        DatasetDetail datasetDetail = new DatasetDetail();
        datasetDetail.setGid(IdGenerator.getInstance().nextId());
        datasetDetail.setName("dataset");
        datasetDetail.setDatabase("test");
        datasetDetail.setDatasource("hive");
        datasetDetail.setSchema("schema");
        datasetDetail.setDescription("desc");
        datasetDetail.setType("Hive");
        datasetDetail.setHighWatermark(new Watermark());
        datasetDetail.setHighWatermark(new Watermark());
        datasetDetail.setOwners(ImmutableList.of("admin"));
        datasetDetail.setTags(ImmutableList.of("tag1"));
        datasetDetail.setDeleted(false);
        return datasetDetail;
    }

}
