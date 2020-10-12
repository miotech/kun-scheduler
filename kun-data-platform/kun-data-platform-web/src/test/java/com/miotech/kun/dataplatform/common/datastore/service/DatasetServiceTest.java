package com.miotech.kun.dataplatform.common.datastore.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.datastore.vo.DatasetSearchRequest;
import com.miotech.kun.dataplatform.common.datastore.vo.DatasetVO;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDatasetProps;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.security.testing.WithMockTestUser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@WithMockTestUser
public class DatasetServiceTest extends AppTestBase {

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Test
    public void searchDatasets_withDefitionIds() {

        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        List<TaskDatasetProps> outputDatasets = taskDefinition.getTaskPayload()
                .getScheduleConfig().getOutputDatasets();

        taskDefinitionDao.create(taskDefinition);
        // invocation
        datasetService.createTaskDatasets(taskDefinition.getDefinitionId(), outputDatasets);

        // verify
        TaskDatasetProps dataset = outputDatasets.get(0);
        DatasetSearchRequest request = new DatasetSearchRequest(
                Collections.singletonList(dataset.getDefinitionId()),
                null,
                null
        );

        List<DatasetVO> vo = datasetService.searchDatasets(request);
        assertThat(vo.size(), is(1));
        assertThat(vo.get(0).getName(), is(dataset.getDatasetName()));
        assertThat(vo.get(0).getDatastoreId(), is(dataset.getDatastoreId()));
        assertThat(vo.get(0).getTaskDefinitions().get(0).getId(), is(taskDefinition.getDefinitionId()));
        assertThat(vo.get(0).getTaskDefinitions().get(0).getName(), is(taskDefinition.getName()));
    }

    @Test
    public void searchDatasets_withDatastoreids() {

        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        List<TaskDatasetProps> outputDatasets = taskDefinition.getTaskPayload()
                .getScheduleConfig().getOutputDatasets();

        taskDefinitionDao.create(taskDefinition);
        // invocation
        datasetService.createTaskDatasets(taskDefinition.getDefinitionId(), outputDatasets);

        // verify
        TaskDatasetProps dataset = outputDatasets.get(0);
        DatasetSearchRequest request = new DatasetSearchRequest(
                null,
                Collections.singletonList(dataset.getDatastoreId()),
                null
        );

        List<DatasetVO> vo = datasetService.searchDatasets(request);
        assertThat(vo.size(), is(1));
        assertThat(vo.get(0).getName(), is(dataset.getDatasetName()));
        assertThat(vo.get(0).getDatastoreId(), is(dataset.getDatastoreId()));
        assertThat(vo.get(0).getTaskDefinitions().get(0).getId(), is(taskDefinition.getDefinitionId()));
        assertThat(vo.get(0).getTaskDefinitions().get(0).getName(), is(taskDefinition.getName()));
    }

    @Test
    public void searchDatasets_withName() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        List<TaskDatasetProps> outputDatasets = taskDefinition.getTaskPayload()
                .getScheduleConfig().getOutputDatasets();

        taskDefinitionDao.create(taskDefinition);
        // invocation
        datasetService.createTaskDatasets(taskDefinition.getDefinitionId(), outputDatasets);

        // verify
        TaskDatasetProps dataset = outputDatasets.get(0);
        DatasetSearchRequest request = new DatasetSearchRequest(
                null,
                null,
                dataset.getDatasetName()
        );

        List<DatasetVO> vo = datasetService.searchDatasets(request);
        assertThat(vo.size(), is(1));
        assertThat(vo.get(0).getName(), is(dataset.getDatasetName()));
        assertThat(vo.get(0).getDatastoreId(), is(dataset.getDatastoreId()));
        assertThat(vo.get(0).getTaskDefinitions().get(0).getId(), is(taskDefinition.getDefinitionId()));
        assertThat(vo.get(0).getTaskDefinitions().get(0).getName(), is(taskDefinition.getName()));
    }
}