package com.miotech.kun.dataplatform.web.controller;

import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.web.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.CreateTaskDefinitionRequest;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TaskDefinitionControllerTest extends AppTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskDefinitionService taskDefinitionService;

    @Test
    public void createTaskDefinition() throws Exception {
        String url = "/task-definitions";

        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        CreateTaskDefinitionRequest taskDefinitionProps = new CreateTaskDefinitionRequest(taskDefinition.getName(), taskDefinition.getTaskTemplateName());

        //TODO: solve mock problem
        Mockito.when(taskDefinitionService.create(any(CreateTaskDefinitionRequest.class))).thenReturn(taskDefinition);
        Mockito.when(taskDefinitionService.convertToVO(any(TaskDefinition.class))).thenCallRealMethod();

        MvcResult mvcResult = mockMvc.perform(post(url)
                .content(JSONUtils.toJsonString(taskDefinitionProps))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        System.out.println(response.getContentAsString());

        //only verify http request and response success
        assertThat(response.getStatus(), is(200));
        verify(taskDefinitionService).create(any(CreateTaskDefinitionRequest.class));
    }

    @Test
    public void searchAllTaskDefinitions() {
    }

    @Test
    public void listAllTaskDefinitions() {
    }

    @Test
    public void updateTaskDefinitionDetail() {
    }

    @Test
    public void getTaskDefinitionDetail() {
    }

    @Test
    public void deleteTaskDefinitionDetail() {
    }

    @Test
    public void checkTaskDefinition() {
    }

    @Test
    public void deployTaskDefinitionDirectly() {
    }

    @Test
    public void runTaskDefinition() {
    }

    @Test
    public void stopTaskDefinitionDetail() {
    }

    @Test
    public void getTaskTry() {
    }

    @Test
    public void getTaskTryLog() {
    }
}