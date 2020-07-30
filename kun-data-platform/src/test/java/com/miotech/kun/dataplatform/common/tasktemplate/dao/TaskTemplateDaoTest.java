package com.miotech.kun.dataplatform.common.tasktemplate.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TaskTemplateDaoTest extends AppTestBase {

    public static final String TEST_TEMPLATE = "SparkSQL";

    @Autowired
    private TaskTemplateDao taskTemplateDao;

    @Test
    public void getTaskTemplates() {
       List<TaskTemplate> taskTemplates = taskTemplateDao.getTaskTemplates();
       assertThat(taskTemplates.size(), is(1));
       assertThat(taskTemplates.get(0).getName(), is(TEST_TEMPLATE));
    }

    @Test
    public void fetchByName() {
        Optional<TaskTemplate> taskTemplate = taskTemplateDao.fetchByName(TEST_TEMPLATE);
        assertTrue(taskTemplate.isPresent());
        assertThat(taskTemplate.get().getName(), is(TEST_TEMPLATE));
    }
}