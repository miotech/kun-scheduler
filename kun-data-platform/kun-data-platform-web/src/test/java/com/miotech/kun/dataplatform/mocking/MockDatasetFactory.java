package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.model.datastore.TaskDataset;

import java.util.ArrayList;
import java.util.List;

public class MockDatasetFactory {
    private MockDatasetFactory() {}

    public static TaskDataset createDataset() {
        return createDatasets(1).get(0);
    }

    public static List<TaskDataset> createDatasets(int num) {
        List<TaskDataset> datasets = new ArrayList<>();

        for (int i=0; i < num; i++) {
            Long datasetId = DataPlatformIdGenerator.nextDatasetId();
            Long definitionId = DataPlatformIdGenerator.nextDefinitionId();

            datasets.add(TaskDataset.newBuilder()
            .withDatasetName("dataset_" + i)
                    .withId(datasetId)
                    .withDatastoreId(1L)
                    .withDefinitionId(definitionId)
            .build());
        }
        return datasets;
    }
}
