package com.miotech.kun.dataplatform.common.tasktemplate.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.tasktemplate.vo.TaskTemplateReqeustVO;
import com.miotech.kun.dataplatform.common.tasktemplate.vo.TaskTemplateVO;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.WorkflowApiException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import static com.miotech.kun.dataplatform.common.tasktemplate.dao.TaskTemplateDaoTest.TEST_TEMPLATE;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO: figure out a solution to bootstrap Workflow facade related tests
@Ignore
public class TaskTemplateServiceTest extends AppTestBase {

    @Autowired
    private TaskTemplateService taskTemplateService;

    @Test
    public void test_createWithInvalidOperatorId() {
        TaskTemplateReqeustVO vo = new TaskTemplateReqeustVO(
              "test",
              "sql",
              "development",
              1L,
                ImmutableList.of(),
                ImmutableMap.of(),
                null
        );
        try {
            taskTemplateService.create(vo);
        } catch (Throwable e){
            assertThat(e.getClass(), is(WorkflowApiException.class));
            assertTrue(e.getMessage().contains("Operator is not found for id"));
        }
    }

    @Test
    public void convertToVO() {
        TaskTemplate taskTemplate = taskTemplateService.find(TEST_TEMPLATE);
        TaskTemplateVO taskTemplateVO = taskTemplateService.convertToVO(taskTemplate);
        assertThat(taskTemplateVO.getName(), is(taskTemplate.getName()));
        assertThat(taskTemplateVO.getTemplateType(), is(taskTemplate.getTemplateType()));
        assertThat(taskTemplateVO.getTemplateGroup(), is(taskTemplate.getTemplateGroup()));
        assertThat(taskTemplateVO.getRenderClassName(), is(taskTemplate.getRenderClassName()));
        assertThat(taskTemplateVO.getDisplayParameters(), sameBeanAs(taskTemplate.getDisplayParameters()));
    }
}