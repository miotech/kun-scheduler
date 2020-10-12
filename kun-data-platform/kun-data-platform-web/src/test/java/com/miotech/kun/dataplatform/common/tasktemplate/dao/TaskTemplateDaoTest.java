package com.miotech.kun.dataplatform.common.tasktemplate.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.mocking.MockTaskTemplateFactory;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void create() {
        TaskTemplate existed = taskTemplateDao.fetchByName(TEST_TEMPLATE).get();
        TaskTemplate taskTemplate = MockTaskTemplateFactory.createTaskTemplate(existed.getOperator().getId());

        taskTemplateDao.create(taskTemplate);
        TaskTemplate fetched = taskTemplateDao.fetchByName(taskTemplate.getName()).get();

        assertThat(fetched.getName(), is(taskTemplate.getName()));
        assertThat(fetched.getOperator().getId(), is(taskTemplate.getOperator().getId()));
        assertThat(fetched.getTemplateType(), is(taskTemplate.getTemplateType()));
        assertThat(fetched.getTemplateGroup(), is(taskTemplate.getTemplateGroup()));
        assertThat(fetched.getDefaultValues(), sameBeanAs(taskTemplate.getDefaultValues()));
        assertThat(fetched.getDisplayParameters(), sameBeanAs(taskTemplate.getDisplayParameters()));
    }

    @Test
    public void update() {
        TaskTemplate taskTemplate = taskTemplateDao.fetchByName(TEST_TEMPLATE).get();
        TaskTemplate updatedTaskTemplate = taskTemplate.cloneBuilder()
                .withTemplateGroup("test-group")
                .build();
        taskTemplateDao.update(updatedTaskTemplate);
        TaskTemplate fetched = taskTemplateDao.fetchByName(updatedTaskTemplate.getName()).get();

        assertThat(fetched.getName(), is(updatedTaskTemplate.getName()));
        assertThat(fetched.getOperator().getId(), is(updatedTaskTemplate.getOperator().getId()));
        assertThat(fetched.getTemplateType(), is(updatedTaskTemplate.getTemplateType()));
        assertThat(fetched.getTemplateGroup(), is(updatedTaskTemplate.getTemplateGroup()));
        assertThat(fetched.getDefaultValues(), sameBeanAs(updatedTaskTemplate.getDefaultValues()));
        assertThat(fetched.getDisplayParameters(), sameBeanAs(updatedTaskTemplate.getDisplayParameters()));
    }
}