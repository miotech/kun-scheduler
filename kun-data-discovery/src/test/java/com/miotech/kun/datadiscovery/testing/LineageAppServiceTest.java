package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.datadiscovery.model.bo.LineageGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.LineageTasksRequest;
import com.miotech.kun.datadiscovery.model.entity.LineageGraph;
import com.miotech.kun.datadiscovery.model.entity.LineageTask;
import com.miotech.kun.datadiscovery.service.DatasetService;
import com.miotech.kun.datadiscovery.service.LineageAppService;
import com.miotech.kun.datadiscovery.testing.mockdata.MockWorkFlowClientDataFactory;
import com.miotech.kun.workflow.client.LineageQueryDirection;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static com.miotech.kun.datadiscovery.service.LineageAppService.LATEST_TASK_LIMIT;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @program: kun
 * @description: testing
 * @author: zemin  huang
 * @create: 2022-01-24 09:20
 **/
public class LineageAppServiceTest extends DiscoveryTestBase {

    @Autowired
    LineageAppService lineageAppService;


    @MockBean
    WorkflowClient workflowClient;
    @MockBean
    DatasetService datasetService;



    private LineageTasksRequest getLineageTasksRequest() {
        LineageQueryDirection lineageQueryDirection = LineageQueryDirection.UPSTREAM;
        LineageTasksRequest lineageTasksRequest = new LineageTasksRequest();
        lineageTasksRequest.setDatasetGid(1L);
        lineageTasksRequest.setDirection(lineageQueryDirection.name());
        lineageTasksRequest.setDestDatasetGid(1L);
        lineageTasksRequest.setSourceDatasetGid(1L);
        return lineageTasksRequest;
    }

    @Test
    void testGetLineageTasksByEdgeInfo_fetch_null() {
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();
        when(workflowClient.getLineageEdgeInfo(lineageTasksRequest.getSourceDatasetGid(), lineageTasksRequest.getDestDatasetGid())).thenReturn(null);
        when(workflowClient.getLatestTaskRuns(mock(List.class), LATEST_TASK_LIMIT, false)).thenReturn(null);
        List<LineageTask> taskList = lineageAppService.getLineageTasksByEdgeInfo(lineageTasksRequest);
        Assert.assertTrue(CollectionUtils.isEmpty(taskList));

    }

    @Test
    void testGetLineageTasksByEdgeInfo_fetch_entity() {
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();
        doReturn(MockWorkFlowClientDataFactory.createEdgeInfo())
                .when(workflowClient).getLineageEdgeInfo(anyLong(), anyLong());
        doReturn(MockWorkFlowClientDataFactory.createLatestTaskRuns())
                .when(workflowClient).getLatestTaskRuns(anyList(), anyInt(), anyBoolean());
        List<LineageTask> taskList = lineageAppService.getLineageTasksByEdgeInfo(lineageTasksRequest);
        Assert.assertTrue(CollectionUtils.isNotEmpty(taskList));

    }


    @Test
    void testGetLineageTasksByNeighbors_fetch_null() {
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();
        when(workflowClient.getLineageNeighbors(lineageTasksRequest.getDatasetGid(),
                        LineageQueryDirection.valueOf(lineageTasksRequest.getDirection()), 1))
                .thenReturn(null);
        when(workflowClient.getLatestTaskRuns(mock(List.class), LATEST_TASK_LIMIT, false)).thenReturn(null);
        List<LineageTask> taskList = lineageAppService.getLineageTasksByNeighbors(lineageTasksRequest);
        Assert.assertTrue(CollectionUtils.isEmpty(taskList));
    }


    @Test
    void testGetLineageTasksByNeighbors_fetch_entity() {
        LineageTasksRequest lineageTasksRequest = getLineageTasksRequest();
        doReturn(MockWorkFlowClientDataFactory.createLineageNeighbors())
                .when(workflowClient).getLineageNeighbors(anyLong(), any(LineageQueryDirection.class),anyInt());
        doReturn(MockWorkFlowClientDataFactory.createLatestTaskRuns())
                .when(workflowClient).getLatestTaskRuns(anyList(), anyInt(), anyBoolean());
        List<LineageTask> taskList = lineageAppService.getLineageTasksByNeighbors(lineageTasksRequest);
        Assert.assertTrue(CollectionUtils.isNotEmpty(taskList));
    }


    @Test
    void testGetLineageGraph_fetch_entity() {

        LineageGraphRequest request =new LineageGraphRequest();
        request.setDatasetGid(1L);
        request.setDepth(1);
        request.setDirection("UPSTREAM");
        doReturn(MockWorkFlowClientDataFactory.createLineageNeighborsGraph())
                .when(workflowClient).getLineageNeighbors(anyLong(), any(LineageQueryDirection.class),anyInt());
        doReturn(MockWorkFlowClientDataFactory.createLineageDatasetBasicList()).when(datasetService).getDatasets(anyList());
        LineageGraph lineageGraph = lineageAppService.getLineageGraph(request);
        Assert.assertNotNull(lineageGraph);



    }
}
