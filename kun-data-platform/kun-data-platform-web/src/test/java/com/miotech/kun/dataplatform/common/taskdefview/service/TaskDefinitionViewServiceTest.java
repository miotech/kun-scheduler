package com.miotech.kun.dataplatform.common.taskdefview.service;

import com.google.common.collect.Lists;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewCreateInfoVO;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TaskDefinitionViewServiceTest extends AppTestBase {

    @Autowired
    TaskDefinitionDao taskDefinitionDao;

    @Autowired
    TaskDefinitionViewService taskDefinitionViewService;

    @Test
    public void create_taskDefViewWithValidCreationInfo_shouldSuccess() {
        // Prepare
        OffsetDateTime mockCurrentTime = DateTimeUtils.freeze();
        List<TaskDefinition> mockTaskDefs = MockTaskDefinitionFactory.createTaskDefinitions(5);
        mockTaskDefs.forEach(taskDef -> {
            taskDefinitionDao.create(taskDef);
        });
        List<Long> taskDefIds = mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toList());
        TaskDefinitionViewCreateInfoVO createInfoVO = new TaskDefinitionViewCreateInfoVO(
                "view_demo",   // name
                1L,          // creator id
                taskDefIds          // task definition ids
        );

        // Process
        TaskDefinitionView persistedView = taskDefinitionViewService.createTaskDefinitionView(createInfoVO);

        // Validate
        assertThat(persistedView.getName(), is(createInfoVO.getName()));
        assertThat(persistedView.getCreator(), is(createInfoVO.getCreator()));
        assertThat(persistedView.getLastModifier(), is(createInfoVO.getCreator()));
        assertThat(persistedView.getCreateTime(), is(mockCurrentTime));
        assertThat(persistedView.getUpdateTime(), is(mockCurrentTime));

        // Teardown
        DateTimeUtils.resetClock();
    }

    @Test
    public void create_withInvalidTaskDefinitionIds_shouldThrowException() {
        // Prepare
        TaskDefinitionViewCreateInfoVO createInfoVO = new TaskDefinitionViewCreateInfoVO(
                "view_demo",  // name
                1L,          // creator id
                Lists.newArrayList(100L, 200L)  // task definition ids (not exists)
        );

        // Process
        try {
            taskDefinitionViewService.createTaskDefinitionView(createInfoVO);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void fetch_existingTaskDefView_shouldFetchAndReturnTheSameModel() {
        // Prepare
        TaskDefinitionViewCreateInfoVO createInfoVO = new TaskDefinitionViewCreateInfoVO(
                "view_demo",  // name
                1L,          // creator id
                new ArrayList<>() // task definition ids
        );
        TaskDefinitionView createdView = taskDefinitionViewService.createTaskDefinitionView(createInfoVO);

        // Process
        Optional<TaskDefinitionView> fetchedView = taskDefinitionViewService.fetchTaskDefinitionViewById(createdView.getId());

        // Validate
        assertTrue(fetchedView.isPresent());
        assertThat(fetchedView.get(), sameBeanAs(createdView));
    }

    @Test
    public void fetch_nonExistingTaskDefView_shouldReturnEmptyOptionalObject() {
        // Process
        Optional<TaskDefinitionView> fetchedView = taskDefinitionViewService.fetchTaskDefinitionViewById(1234L);

        // Validate
        assertFalse(fetchedView.isPresent());
    }

    @Test
    public void save_nonExistingTaskDefView_shouldPerformCreateAction() {
        // Prepare
        OffsetDateTime mockCurrentTime = DateTimeUtils.freeze();
        TaskDefinitionView viewNotPersisted = TaskDefinitionView.newBuilder()
                .withId(1234L)
                .withName("view_demo")
                .withCreateTime(mockCurrentTime)
                .withUpdateTime(mockCurrentTime)
                .withCreator(1L)
                .withLastModifier(1L)
                .withIncludedTaskDefinitions(new ArrayList<>())
                .build();

        // Process
        Optional<TaskDefinitionView> viewFetchedBeforeSave = taskDefinitionViewService.fetchTaskDefinitionViewById(1234L);
        TaskDefinitionView savedView = taskDefinitionViewService.saveTaskDefinitionView(viewNotPersisted);
        Optional<TaskDefinitionView> viewFetchedAfterSave = taskDefinitionViewService.fetchTaskDefinitionViewById(1234L);

        // Validate
        assertFalse(viewFetchedBeforeSave.isPresent());
        assertTrue(viewFetchedAfterSave.isPresent());
        assertThat(savedView, sameBeanAs(viewNotPersisted));
        assertThat(viewFetchedAfterSave.get(), sameBeanAs(savedView));

        // Teardown
        DateTimeUtils.resetClock();
    }
}
