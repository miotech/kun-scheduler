package com.miotech.kun.dataplatform.web.common.tasktemplate.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.web.common.tasktemplate.service.TaskTemplateLoader;
import com.miotech.kun.dataplatform.web.model.tasktemplate.ParameterDefinition;
import com.miotech.kun.dataplatform.web.model.tasktemplate.TaskTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskTemplateLoaderTest extends AppTestBase {

    @Autowired
    private TaskTemplateLoader loader;

    @Test
    public void getTaskTemplates() {
        List<TaskTemplate> taskTemplateList = loader.getTaskTemplates();

        assertThat(taskTemplateList.size(), is(1));
        TaskTemplate taskTemplate = taskTemplateList.get(0);
        assertThat(taskTemplate.getName(), is("SparkSQL"));
        assertThat(taskTemplate.getTemplateType(), is("sql"));
        assertThat(taskTemplate.getTemplateGroup(), is("development"));
        assertTrue(taskTemplate.getOperator().getId() > 0);
        assertThat(taskTemplate.getOperator().getConfigDef().size(), is(1));
        assertThat(taskTemplate.getDisplayParameters().size(), is(1));

        ParameterDefinition parameterDefinition = taskTemplate.getDisplayParameters().get(0);
        assertThat(parameterDefinition.getName(), is("sparkSQL"));
        assertThat(parameterDefinition.getDisplayName(), is("sql"));
        assertThat(parameterDefinition.getType(), is("sql"));
        assertThat(parameterDefinition.isRequired(), is(true));
    }
}