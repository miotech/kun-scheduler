package com.miotech.kun.datadiscovery.testing;

import com.miotech.kun.datadiscovery.model.bo.LineageTasksRequest;
import com.miotech.kun.datadiscovery.model.entity.LineageTask;
import com.miotech.kun.datadiscovery.service.LineageAppService;
import com.miotech.kun.workflow.client.LineageQueryDirection;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.miotech.kun.datadiscovery.service.LineageAppService.LATEST_TASK_LIMIT;

/**
 * @program: kun
 * @description: testing
 * @author: zemin  huang
 * @create: 2022-01-24 09:20
 **/
public class LineageAppServiceTest  extends  DiscoveryTestBase{

    @Autowired
    LineageAppService lineageAppService;

    @Test
    void getLineageGraph() {

    }

    @MockBean
    WorkflowClient workflowClient;

    @Test
    void testGetLineageTasksResultBySourceDatasetGid_fetch_null() {
        Long sourceSetGid=64277L;
        Long destSetGid=6425L;
        LineageTasksRequest lineageTasksRequest = new LineageTasksRequest();
        lineageTasksRequest.setDestDatasetGid(destSetGid);
        lineageTasksRequest.setSourceDatasetGid(sourceSetGid);
        Mockito.when(workflowClient.getLineageEdgeInfo(sourceSetGid,destSetGid)).thenReturn(null);
        List<LineageTask> taskList = lineageAppService.getLineageTasksResultBySourceDatasetGid(lineageTasksRequest);
        Assertions.assertTrue(CollectionUtils.isEmpty(taskList));

    }

    @Test
    void testGetLineageTasksResultByDatasetGid_UPSTREAM() {
        Long gid=100000000L;
        LineageQueryDirection lineageQueryDirection = LineageQueryDirection.UPSTREAM;
        LineageTasksRequest lineageTasksRequest = new LineageTasksRequest();
        lineageTasksRequest.setDatasetGid(gid);
        lineageTasksRequest.setDirection(lineageQueryDirection.name());
        Mockito.when(workflowClient.getLineageNeighbors(gid,lineageQueryDirection,1)).thenReturn(null);
        Mockito.when(workflowClient.getLatestTaskRuns(Mockito.mock(List.class),LATEST_TASK_LIMIT,false)).thenReturn(null);
        List<LineageTask> taskList = lineageAppService.getLineageTasksResultByDatasetGid(lineageTasksRequest);
        Assertions.assertTrue(CollectionUtils.isEmpty(taskList));
    }

}
