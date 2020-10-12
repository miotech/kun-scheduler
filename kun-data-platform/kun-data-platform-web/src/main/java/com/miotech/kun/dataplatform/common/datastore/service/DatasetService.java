package com.miotech.kun.dataplatform.common.datastore.service;

import com.miotech.kun.dataplatform.common.datastore.dao.DatasetDao;
import com.miotech.kun.dataplatform.common.datastore.vo.DatasetSearchRequest;
import com.miotech.kun.dataplatform.common.datastore.vo.DatasetVO;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefinition.vo.TaskDefinitionProps;
import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.model.datastore.TaskDataset;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDatasetProps;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DatasetService {

    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private TaskDefinitionDao definitionDao;

    public TaskDataset create(Long definitionId, Long datastoreId, String datasetName) {
        Long datasetId = DataPlatformIdGenerator.nextDatasetId();
        TaskDataset dataset = TaskDataset.newBuilder()
                .withId(datasetId)
                .withDefinitionId(definitionId)
                .withDatastoreId(datastoreId)
                .withDatasetName(datasetName)
                .build();
        datasetDao.create(dataset);
        return dataset;
    }

    public List<DatasetVO> searchDatasets(DatasetSearchRequest request) {
        List<TaskDataset> datasets = datasetDao.search(request);
        List<TaskDefinition> definitions = definitionDao.fetchByIds(
                datasets.stream().map(TaskDataset::getDefinitionId)
                        .collect(Collectors.toList())
        );

        Map<Long, TaskDefinition> definitionMap = definitions.stream()
                .collect(Collectors.toMap(TaskDefinition::getDefinitionId,
                        Function.identity()));

        // group by datastoreId and name
        List<DatasetVO> vos = datasets
        .stream()
        .collect(Collectors.groupingBy(TaskDataset::getDatastoreId,
                Collectors.groupingBy(TaskDataset::getDatasetName, Collectors.toSet())))
                .entrySet()
                .stream()
                .flatMap( x -> x.getValue().entrySet().stream()
                .map( t -> new DatasetVO(
                        x.getKey(),
                        t.getKey(),
                        t.getValue().stream().map(s -> new TaskDefinitionProps(
                                s.getDefinitionId(),
                                definitionMap.get(s.getDefinitionId()).getName()
                        )).collect(Collectors.toList())
                ) ))
        .collect(Collectors.toList());
        return vos;
    }

    @Transactional
    public List<TaskDataset> createTaskDatasets(Long definitionId, List<TaskDatasetProps> datasetProps) {
        List<TaskDataset> taskDatasets = datasetProps.stream()
                .map( x -> TaskDataset.newBuilder()
                            .withId(DataPlatformIdGenerator.nextDatasetId())
                            .withDefinitionId(definitionId)
                            .withDatastoreId(x.getDatastoreId())
                            .withDatasetName(x.getDatasetName())
                            .build())
                .collect(Collectors.toList());

        datasetDao.updateTaskDatasets(definitionId, taskDatasets);
        return taskDatasets;
    }
}
