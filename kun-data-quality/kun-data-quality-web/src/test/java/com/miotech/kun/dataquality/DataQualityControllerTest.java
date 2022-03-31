package com.miotech.kun.dataquality;

import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.common.constant.ErrorCode;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataquality.mock.MockDatasetBasicFactory;
import com.miotech.kun.dataquality.mock.MockExpectationRequestFactory;
import com.miotech.kun.dataquality.mock.MockExpectationsRequestFactory;
import com.miotech.kun.dataquality.mock.MockOperatorFactory;
import com.miotech.kun.dataquality.web.model.bo.ExpectationRequest;
import com.miotech.kun.dataquality.web.model.bo.ExpectationsRequest;
import com.miotech.kun.dataquality.web.model.entity.DatasetBasic;
import com.miotech.kun.dataquality.web.model.entity.ExpectationBasic;
import com.miotech.kun.dataquality.web.model.entity.ExpectationBasics;
import com.miotech.kun.dataquality.web.model.entity.ExpectationVO;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import com.miotech.kun.dataquality.web.service.DataQualityService;
import com.miotech.kun.dataquality.web.service.WorkflowService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.task.CheckType;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DataQualityControllerTest extends DataQualityTestBase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DataQualityService dataQualityService;

    @SpyBean
    private WorkflowService workflowService;

    @SpyBean
    private DatasetRepository datasetRepository;

    @MockBean
    private WorkflowClient workflowClient;

    @BeforeEach
    public void mock() {
        doAnswer(invocation -> {
            Long taskId = invocation.getArgument(0, Long.class);
            TaskRun taskRun = TaskRun.newBuilder().withTask(Task.newBuilder().withId(taskId).build())
                    .withId(WorkflowIdGenerator.nextTaskRunId()).build();
            return taskRun;
        }).when(workflowClient).executeTask(anyLong(), any());

        doAnswer(invocation -> {
            Task task = invocation.getArgument(0, Task.class);
            Task createdTask = task.cloneBuilder().withId(WorkflowIdGenerator.nextTaskId()).build();
            return createdTask;
        }).when(workflowClient).createTask(any(Task.class));

        doReturn(MockOperatorFactory.createOperator())
                .when(workflowClient)
                .saveOperator(anyString(), any());

        doReturn(Optional.of(MockOperatorFactory.createOperator())).when(workflowClient).getOperator(anyString());

        doReturn(MockOperatorFactory.createOperator()).when(workflowClient).getOperator(anyLong());
    }

    @Test
    public void testGetById() throws Exception {
        // mock
        doNothing().when(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));
        doReturn(1L).when(datasetRepository).findDataSourceIdByGid(anyLong());

        // prepare
        ExpectationRequest expectationRequest = MockExpectationRequestFactory.create();
        Long expectationId = dataQualityService.createExpectation(expectationRequest);

        DatasetBasic datasetBasic = MockDatasetBasicFactory.create();
        doReturn(datasetBasic).when(datasetRepository).findBasic(expectationRequest.getMetrics().getDatasetGid());
        String findByIdUrl = "/kun/api/v1/data-quality/" + expectationId;
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(findByIdUrl).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        // assert
        assertThat(response.getStatus(), is(200));
        String contentAsString = response.getContentAsString();
        RequestResult<ExpectationVO> expectationVORequestResult = JSONUtils.jsonToObject(contentAsString, new TypeReference<RequestResult<ExpectationVO>>() {
        });
        assertThat(expectationVORequestResult.getCode(), is(ErrorCode.SUCCESS.getCode()));
        ExpectationVO expectationVO = expectationVORequestResult.getResult();
        assertThat(expectationVO.getName(), is(expectationRequest.getName()));
        assertThat(expectationVO.getTypes(), is(expectationRequest.getTypes()));
        assertThat(expectationVO.getDescription(), is(expectationRequest.getDescription()));
        assertThat(expectationVO.getMetrics(), sameBeanAs(expectationRequest.getMetrics()));
        assertThat(expectationVO.getAssertion(), sameBeanAs(expectationRequest.getAssertion()));
    }

    @Test
    public void testGetByDatasetGid() throws Exception {
        // mock
        doNothing().when(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));
        doReturn(1L).when(datasetRepository).findDataSourceIdByGid(anyLong());

        // prepare
        ExpectationRequest expectationRequest = MockExpectationRequestFactory.create();
        dataQualityService.createExpectation(expectationRequest);

        String findByIdUrl = "/kun/api/v1/data-qualities?gid=%s";
        ExpectationsRequest expectationsRequest = MockExpectationsRequestFactory.create(expectationRequest.getMetrics().getDatasetGid());
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(String.format(findByIdUrl, expectationsRequest.getGid()))
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();

        // assert
        MockHttpServletResponse response = mvcResult.getResponse();
        assertThat(response.getStatus(), is(200));
        String contentAsString = response.getContentAsString();
        RequestResult<ExpectationBasics> expectationBasicsRequestResult = JSONUtils.jsonToObject(contentAsString, new TypeReference<RequestResult<ExpectationBasics>>() {
        });
        assertThat(expectationBasicsRequestResult.getCode(), is(ErrorCode.SUCCESS.getCode()));
        ExpectationBasics expectationBasics = expectationBasicsRequestResult.getResult();
        assertThat(expectationBasics.getExpectationBasics().size(), is(1));
        ExpectationBasic expectationBasic = expectationBasics.getExpectationBasics().get(0);
        assertThat(expectationBasic.getName(), is(expectationRequest.getName()));
        assertThat(expectationBasic.getTypes(), is(expectationRequest.getTypes()));
        assertThat(expectationBasic.getDescription(), is(expectationRequest.getDescription()));
        assertThat(expectationBasic.getCaseType(), is(expectationRequest.getCaseType()));
    }

    @Test
    public void updateIsBlocking_shouldCallWorkflowApi() throws Exception {
        // mock
        doNothing().when(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));
        doReturn(1L).when(datasetRepository).findDataSourceIdByGid(anyLong());

        // prepare
        ExpectationRequest expectationRequest = MockExpectationRequestFactory.create();
        Long expectationId = dataQualityService.createExpectation(expectationRequest);
        String url = "/kun/api/v1/data-quality/" + expectationId + "/edit";

        String content = JSONUtils.toJsonString(expectationRequest);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(url)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        //verify
        int status = response.getStatus();
        assertThat(status, is(200));
        verify(workflowService, times(2)).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));
    }

    @Test
    public void addCaseWithIsBlocking_shouldCallWorkflowApi() throws Exception {
        // prepare
        String url = "/kun/api/v1/data-quality/add";
        doNothing().when(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));
        doReturn(1L).when(datasetRepository).findDataSourceIdByGid(anyLong());

        String content = JSONUtils.toJsonString(MockExpectationRequestFactory.create());
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        //verify
        int status = response.getStatus();
        assertThat(status, is(200));
        verify(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));
    }

}
