package com.miotech.kun.metadata.web.controller;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.common.service.LineageService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestRequest;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestResponse;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetNode;
import com.miotech.kun.workflow.core.model.lineage.node.TaskNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class DatasetController {

    private static final Logger logger = LoggerFactory.getLogger(DatasetController.class);

    @Inject
    private MetadataDatasetService datasetService;

    @Inject
    private LineageService lineageService;

    @RouteMapping(url = "/dataset/database/_suggest", method = "GET")
    public List<String> suggestDatabase(@QueryParameter String prefix) {
        logger.debug("Suggest database, prefix: {}", prefix);
        return datasetService.suggestDatabase(prefix);
    }

    @RouteMapping(url = "/dataset/table/_suggest", method = "GET")
    public List<String> suggestTable(@QueryParameter(required = true) String databaseName, @QueryParameter String prefix) {
        logger.debug("Suggest table, databaseName: {}, prefix: {}", databaseName, prefix);
        return datasetService.suggestTable(databaseName, prefix);
    }

    @RouteMapping(url = "/dataset/column/_suggest", method = "POST")
    public List<DatasetColumnSuggestResponse> suggestColumn(@RequestBody List<DatasetColumnSuggestRequest> columnSuggestRequests) {
        logger.debug("Suggest column, columnSuggestRequests: {}", columnSuggestRequests);
        String checkParamResult = check(columnSuggestRequests);
        if (StringUtils.isNotBlank(checkParamResult)) {
            throw new IllegalArgumentException(checkParamResult);
        }

        return datasetService.suggestColumn(columnSuggestRequests);
    }

    /**
     * Get all task witch produce this data set as it's output
     * by datasetId
     * @param datasetId
     * @return
     */
    @RouteMapping(url = "/dataset/task/upstream", method = "GET")
    List<Long> getUpstreamTaskIdByDataSetId(@QueryParameter Long datasetId){
        Optional<DatasetNode> datasetNodeOptional = lineageService.fetchDatasetNodeById(datasetId);
        if(datasetNodeOptional.isPresent()){
            DatasetNode datasetNode = datasetNodeOptional.get();
            return datasetNode.getUpstreamTasks().stream().map(TaskNode::getTaskId).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * Get all task witch use this data set as it's input
     * by datasetId
     * @param datasetId
     * @return
     */
    @RouteMapping(url = "/dataset/task/downstream", method = "GET")
    List<Long> getDownStreamTaskIdByDataSetId(@QueryParameter Long datasetId){
        Optional<DatasetNode> datasetNodeOptional = lineageService.fetchDatasetNodeById(datasetId);
        if(datasetNodeOptional.isPresent()){
            DatasetNode datasetNode = datasetNodeOptional.get();
            return datasetNode.getDownstreamTasks().stream().map(TaskNode::getTaskId).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * Get all dataset product by given taskId
     * @param taskId
     * @return
     */
    @RouteMapping(url = "/dataset/output", method = "GET")
    List<Dataset> getOutputDataSetByTaskId(@QueryParameter Long taskId){
        Set<DatasetNode> outputDataSetNodes =  lineageService.fetchOutletNodes(taskId);
        List<Dataset> outputDataSets = new ArrayList<>();
        for (DatasetNode datasetNode : outputDataSetNodes){
            Optional<Dataset> datasetOptional = datasetService.fetchDatasetByGid(datasetNode.getGid());
            if(datasetOptional.isPresent()){
                outputDataSets.add(datasetOptional.get());
            }
        }
        return outputDataSets;
    }

    /**
     * Get all dataset used as input by given taskId
     * @param taskId
     * @return
     */
    @RouteMapping(url = "/dataset/input", method = "GET")
    List<Dataset> getInputDataSetByTaskId(@QueryParameter Long taskId){
        Set<DatasetNode> inputDataSetNodes =  lineageService.fetchInletNodes(taskId);
        List<Dataset> inputDataSets = new ArrayList<>();
        for (DatasetNode datasetNode : inputDataSetNodes){
            Optional<Dataset> datasetOptional = datasetService.fetchDatasetByGid(datasetNode.getGid());
            if(datasetOptional.isPresent()){
                inputDataSets.add(datasetOptional.get());
            }
        }
        return inputDataSets;
    }

    private String check(List<DatasetColumnSuggestRequest> columnSuggestRequests) {
        if (CollectionUtils.isEmpty(columnSuggestRequests)) {
            return "requests should not be empty";
        }

        for (DatasetColumnSuggestRequest columnSuggestRequest : columnSuggestRequests) {
            if (StringUtils.isBlank(columnSuggestRequest.getDatabaseName())) {
                return "databaseName should not be empty";
            }

            if (StringUtils.isBlank(columnSuggestRequest.getTableName())) {
                return "tableName should not be empty";
            }
        }

        return StringUtils.EMPTY;
    }

}
