package com.miotech.kun.dataplatform.common.datastore.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.mocking.MockDatasetFactory;
import com.miotech.kun.dataplatform.model.datastore.TaskDataset;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DatasetDaoTest extends AppTestBase {

    @Autowired
    private DatasetDao datasetDao;

    @Test
    public void create() {
        TaskDataset taskDataset = MockDatasetFactory.createDataset();
        datasetDao.create(taskDataset);

        TaskDataset fetched = datasetDao.fetchById(taskDataset.getId()).get();
        assertThat(fetched.getId(), is(taskDataset.getId()));
        assertThat(fetched.getDatasetName(), is(taskDataset.getDatasetName()));
        assertThat(fetched.getDefinitionId(), is(taskDataset.getDefinitionId()));
        assertThat(fetched.getDatastoreId(), is(taskDataset.getDatastoreId()));
    }

    @Test
    public void fetchDefinitionIdsByName() {
        TaskDataset taskDataset = MockDatasetFactory.createDataset();
        datasetDao.create(taskDataset);

        TaskDataset fetched = datasetDao.fetchByName(taskDataset.getDatasetName()).get(0);
        assertThat(fetched.getId(), is(taskDataset.getId()));
        assertThat(fetched.getDatasetName(), is(taskDataset.getDatasetName()));
        assertThat(fetched.getDefinitionId(), is(taskDataset.getDefinitionId()));
        assertThat(fetched.getDatastoreId(), is(taskDataset.getDatastoreId()));
    }

    @Test
    public void updateTaskDatasets() {
        List<TaskDataset> taskDatasets = MockDatasetFactory.createDatasets(10);
        Long definitionId = DataPlatformIdGenerator.nextDefinitionId();
        taskDatasets = taskDatasets.stream().map(x -> x.cloneBuilder()
                .withDefinitionId(definitionId).build())
                .collect(Collectors.toList());
        datasetDao.updateTaskDatasets(definitionId, taskDatasets);

        List<TaskDataset> fetched = datasetDao.fetchByDefinitionId(definitionId);
        assertThat(fetched.size(), is(taskDatasets.size()));
        assertThat(fetched.get(0), sameBeanAs(taskDatasets.get(0)));
    }
}