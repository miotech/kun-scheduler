package com.miotech.kun.workflow.common.graph;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DirectTaskGraphTest extends DatabaseTestBase {

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;


    @Override
    protected String getFlywayLocation() {
        return "workflow/";
    }

    @Test
    public void testTopoSort(){
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3,"2>>1;1>>0");
        // process
        DirectTaskGraph graph = new DirectTaskGraph(taskList);
        List<Task> sortedTask = graph.tasksScheduledAt(new Tick(DateTimeUtils.now()));
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        Task task3 = taskList.get(2);
        assertThat(sortedTask.get(0).getId(),is(task3.getId()));
        assertThat(sortedTask.get(1).getId(),is(task2.getId()));
        assertThat(sortedTask.get(2).getId(),is(task1.getId()));
    }

}
