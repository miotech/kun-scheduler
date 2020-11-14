package com.miotech.kun.dataplatform.common.taskdefview.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

    private List<TaskDefinition> createMockTaskDefsAndReturn() {
        List<TaskDefinition> mockTaskDefs = MockTaskDefinitionFactory.createTaskDefinitions(5);
        mockTaskDefs.forEach(taskDef -> {
            taskDefinitionDao.create(taskDef);
        });
        return mockTaskDefs;
    }

    private TaskDefinitionView createSimpleMockViewAndReturn() {
        TaskDefinitionViewCreateInfoVO createInfoVO = new TaskDefinitionViewCreateInfoVO(
                "view_demo",        // name
                1L,                 // creator id
                new ArrayList<>()          // task definition ids
        );

        // Return persisted view
        return taskDefinitionViewService.create(createInfoVO);
    }

    @Test
    public void create_taskDefViewWithValidCreationInfo_shouldSuccess() {
        // Prepare
        OffsetDateTime mockCurrentTime = DateTimeUtils.freeze();
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        List<Long> taskDefIds = mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toList());
        TaskDefinitionViewCreateInfoVO createInfoVO = new TaskDefinitionViewCreateInfoVO(
                "view_demo",        // name
                1L,                 // creator id
                taskDefIds          // task definition ids
        );

        // Process
        TaskDefinitionView persistedView = taskDefinitionViewService.create(createInfoVO);

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
                "view_demo",                    // name
                1L,                             // creator id
                Lists.newArrayList(100L, 200L)  // task definition ids (not exists)
        );

        // Process
        try {
            taskDefinitionViewService.create(createInfoVO);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void fetch_existingTaskDefView_shouldFetchAndReturnTheSameModel() {
        // Prepare
        TaskDefinitionView createdView = createSimpleMockViewAndReturn();

        // Process
        Optional<TaskDefinitionView> fetchedView = taskDefinitionViewService.fetchById(createdView.getId());

        // Validate
        assertTrue(fetchedView.isPresent());
        assertThat(fetchedView.get(), sameBeanAs(createdView));
    }

    @Test
    public void fetch_nonExistingTaskDefView_shouldReturnEmptyOptionalObject() {
        // Process
        Optional<TaskDefinitionView> fetchedView = taskDefinitionViewService.fetchById(1234L);

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
        Optional<TaskDefinitionView> viewFetchedBeforeSave = taskDefinitionViewService.fetchById(1234L);
        TaskDefinitionView savedView = taskDefinitionViewService.save(viewNotPersisted);
        Optional<TaskDefinitionView> viewFetchedAfterSave = taskDefinitionViewService.fetchById(1234L);

        // Validate
        assertFalse(viewFetchedBeforeSave.isPresent());
        assertTrue(viewFetchedAfterSave.isPresent());
        assertThat(savedView, sameBeanAs(viewNotPersisted));
        assertThat(viewFetchedAfterSave.get(), sameBeanAs(savedView));

        // Teardown
        DateTimeUtils.resetClock();
    }

    @Test
    public void save_existingTaskDefView_shouldPerformUpdateAction() {
        // Prepare
        DateTimeUtils.freeze();
        TaskDefinitionView viewInit = createSimpleMockViewAndReturn();
        TaskDefinitionView viewUpdate = viewInit.cloneBuilder()
                .withName("view_demo_modified")
                .withLastModifier(2L)
                .build();

        // Process
        Optional<TaskDefinitionView> viewFetchedBeforeSave = taskDefinitionViewService.fetchById(viewInit.getId());
        TaskDefinitionView savedView = taskDefinitionViewService.save(viewUpdate);
        Optional<TaskDefinitionView> viewFetchedAfterSave = taskDefinitionViewService.fetchById(viewInit.getId());

        // Validate
        assertTrue(viewFetchedBeforeSave.isPresent());
        assertThat(viewFetchedBeforeSave.get().getId(), is(viewInit.getId()));
        assertThat(viewFetchedBeforeSave.get().getName(), is("view_demo"));
        assertThat(viewFetchedBeforeSave.get().getCreator(), is(1L));
        assertThat(viewFetchedBeforeSave.get().getLastModifier(), is(1L));

        assertTrue(viewFetchedAfterSave.isPresent());
        assertThat(viewFetchedAfterSave.get().getId(), is(viewInit.getId()));
        assertThat(viewFetchedAfterSave.get().getName(), is("view_demo_modified"));
        assertThat(viewFetchedAfterSave.get().getCreator(), is(1L));
        assertThat(viewFetchedAfterSave.get().getLastModifier(), is(2L));
        assertThat(viewFetchedAfterSave.get(), sameBeanAs(savedView));

        // Teardown
        DateTimeUtils.resetClock();
    }

    @Test
    public void deleteById_onExistingView_shouldRemoveIt() {
        // Prepare
        TaskDefinitionView viewCreated = createSimpleMockViewAndReturn();

        // Process
        Optional<TaskDefinitionView> viewFetchedBeforeDelete = taskDefinitionViewService.fetchById(viewCreated.getId());
        taskDefinitionViewService.deleteById(viewCreated.getId());
        Optional<TaskDefinitionView> viewFetchedAfterDelete = taskDefinitionViewService.fetchById(viewCreated.getId());

        // Validate
        assertTrue(viewFetchedBeforeDelete.isPresent());
        assertFalse(viewFetchedAfterDelete.isPresent());
    }

    @Test
    public void deleteById_multipleTimes_shouldBeIdempotence() {
        // Prepare
        TaskDefinitionView viewCreated = createSimpleMockViewAndReturn();

        // Process
        boolean flagOnDeleteNonExistingView = taskDefinitionViewService.deleteById(1234L);

        Optional<TaskDefinitionView> viewFetchedBeforeDelete = taskDefinitionViewService.fetchById(viewCreated.getId());
        boolean flagOnDeleteExistingViewFirstTime = taskDefinitionViewService.deleteById(viewCreated.getId());
        boolean flagOnDeleteExistingViewSecondTime = taskDefinitionViewService.deleteById(viewCreated.getId());
        Optional<TaskDefinitionView> viewFetchedAfterDeleteMultipleTimes = taskDefinitionViewService.fetchById(viewCreated.getId());

        // Validate
        assertFalse(flagOnDeleteNonExistingView);
        assertTrue(viewFetchedBeforeDelete.isPresent());
        assertTrue(flagOnDeleteExistingViewFirstTime);
        assertFalse(flagOnDeleteExistingViewSecondTime);
        assertFalse(viewFetchedAfterDeleteMultipleTimes.isPresent());
    }

    @Test
    public void putTaskDefinitionsIntoView_withExistingTaskDefsAndView_shouldWorkProperly() {
        // Prepare
        TaskDefinitionView viewCreated = createSimpleMockViewAndReturn();
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        Long mockModifierId = 2L;

        // Process
        taskDefinitionViewService.putTaskDefinitionsIntoView(
                mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                viewCreated.getId(),
                mockModifierId
        );
        TaskDefinitionView viewAfterModify = taskDefinitionViewService.fetchById(viewCreated.getId())
                .orElseThrow(NullPointerException::new);

        // Validate
        assertThat(viewAfterModify.getIncludedTaskDefinitions().size(), is(mockTaskDefs.size()));
        assertThat(
                viewAfterModify.getIncludedTaskDefinitions().stream()
                        .map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                sameBeanAs(mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()))
        );
        assertThat(viewAfterModify.getLastModifier(), is(mockModifierId));
    }

    @Test
    public void putTaskDefinitionsIntoView_withInvalidTaskDefsOrView_shouldThrowException() {
        // case 1: view not exists
        // Prepare
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        // Process
        try {
            taskDefinitionViewService.putTaskDefinitionsIntoView(
                    mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                    1234L,
                    2L
            );
            // Validate
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }

        // case 2: task definitions not exists
        // Prepare
        TaskDefinitionView viewCreated = createSimpleMockViewAndReturn();
        // Process
        try {
            taskDefinitionViewService.putTaskDefinitionsIntoView(
                    Sets.newHashSet(111L, 222L, 333L),  // task definition ids that do not exist
                    viewCreated.getId(),
                    1L
            );
            // Validate
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void putTaskDefinitionsIntoView_multipleTimes_shouldBeIdempotence() {
        // Prepare
        TaskDefinitionView viewCreated = createSimpleMockViewAndReturn();
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        Long mockModifier1Id = 2L;
        Long mockModifier2Id = 3L;

        // Process
        // Perform same action 2 times, with different modifiers
        taskDefinitionViewService.putTaskDefinitionsIntoView(
                mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                viewCreated.getId(),
                mockModifier1Id
        );
        taskDefinitionViewService.putTaskDefinitionsIntoView(
                mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                viewCreated.getId(),
                mockModifier2Id
        );
        TaskDefinitionView viewAfterModify = taskDefinitionViewService.fetchById(viewCreated.getId())
                .orElseThrow(NullPointerException::new);

        // Validate
        // same task definitions should not appear twice
        assertThat(viewAfterModify.getIncludedTaskDefinitions().size(), is(mockTaskDefs.size()));
        assertThat(
                viewAfterModify.getIncludedTaskDefinitions().stream()
                        .map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                sameBeanAs(mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()))
        );
        // but last modifier should be the latest one
        assertThat(viewAfterModify.getLastModifier(), is(mockModifier2Id));
    }

    @Test
    public void removeTaskDefinitionsFromView_withExistingTaskDefsAndView_shouldWorkProperly() {
    }

    @Test
    public void removeTaskDefinitionsFromView_withInvalidArguments_shouldThrowException() {
    }

    @Test
    public void removeTaskDefinitionsFromView_multipleTimes_shouldBeIdempotence() {
    }
}
