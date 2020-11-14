package com.miotech.kun.dataplatform.common.taskdefview.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewCreateInfoVO;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionViewFactory;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.*;
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
        return createSimpleMockViewAndReturn(new ArrayList<>());
    }

    private TaskDefinitionView createSimpleMockViewAndReturn(List<Long> initTaskDefinitionIds) {
        TaskDefinitionViewCreateInfoVO createInfoVO = TaskDefinitionViewCreateInfoVO.builder()
                .name("view_demo")
                .creator(1L)
                .includedTaskDefinitionIds(initTaskDefinitionIds)
                .build();

        // Return persisted view
        return taskDefinitionViewService.create(createInfoVO);
    }

    @Test
    public void create_taskDefViewWithValidCreationInfo_shouldSuccess() {
        // Prepare
        OffsetDateTime mockCurrentTime = DateTimeUtils.freeze();
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        List<Long> taskDefIds = mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toList());
        TaskDefinitionViewCreateInfoVO createInfoVO = TaskDefinitionViewCreateInfoVO.builder()
                .name("view_demo")
                .creator(1L)
                .includedTaskDefinitionIds(taskDefIds)
                .build();

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
        TaskDefinitionViewCreateInfoVO createInfoVO = TaskDefinitionViewCreateInfoVO.builder()
                .name("view_demo")
                .creator(1L)
                .includedTaskDefinitionIds(Lists.newArrayList(100L, 200L))   // task definition ids (not exists)
                .build();

        // Process
        try {
            taskDefinitionViewService.create(createInfoVO);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void fetchById_existingTaskDefView_shouldFetchAndReturnTheSameModel() {
        // Prepare
        TaskDefinitionView createdView = createSimpleMockViewAndReturn();

        // Process
        Optional<TaskDefinitionView> fetchedView = taskDefinitionViewService.fetchById(createdView.getId());

        // Validate
        assertTrue(fetchedView.isPresent());
        assertThat(fetchedView.get(), sameBeanAs(createdView));
    }

    @Test
    public void fetchById_nonExistingTaskDefView_shouldReturnEmptyOptionalObject() {
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
        // Prepare
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        List<Long> taskDefIdsAtInit = mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toList());
        TaskDefinitionView viewCreated = createSimpleMockViewAndReturn(taskDefIdsAtInit);
        // remove first 2 task defs
        Set<Long> taskDefIdsToRemove = Sets.newHashSet(mockTaskDefs.get(0).getDefinitionId(), mockTaskDefs.get(1).getDefinitionId());
        // compute expected remaining task def ids
        Set<Long> taskDefIdsRemainingExpected = (new HashSet<>(taskDefIdsAtInit));
        taskDefIdsRemainingExpected.removeAll(taskDefIdsToRemove);

        // Process
        taskDefinitionViewService.removeTaskDefinitionsFromView(
                taskDefIdsToRemove,
                viewCreated.getId(),
                2L
        );
        TaskDefinitionView viewAfterModify = taskDefinitionViewService.fetchById(viewCreated.getId())
                .orElseThrow(NullPointerException::new);

        // Validate
        assertThat(viewAfterModify.getIncludedTaskDefinitions().size(), is(taskDefIdsAtInit.size() - taskDefIdsToRemove.size()));
        assertThat(
                viewAfterModify.getIncludedTaskDefinitions().stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                sameBeanAs(taskDefIdsRemainingExpected)
        );
        assertThat(viewAfterModify.getLastModifier(), is(2L));
    }

    @Test
    public void removeTaskDefinitionsFromView_withInvalidArguments_shouldThrowException() {
        // Prepare
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();

        // Process
        try {
            taskDefinitionViewService.removeTaskDefinitionsFromView(
                    mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                    1234L,   // Non-exist view id
                    2L
            );
            // Validate
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void removeTaskDefinitionsFromView_multipleTimes_shouldBeIdempotence() {
        // Prepare
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        List<Long> taskDefIdsAtInit = mockTaskDefs.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toList());
        TaskDefinitionView viewCreated = createSimpleMockViewAndReturn(taskDefIdsAtInit);
        // remove first 2 task defs
        Set<Long> taskDefIdsToRemove = Sets.newHashSet(mockTaskDefs.get(0).getDefinitionId(), mockTaskDefs.get(1).getDefinitionId());
        // compute expected remaining task def ids
        Set<Long> taskDefIdsRemainingExpected = (new HashSet<>(taskDefIdsAtInit));
        taskDefIdsRemainingExpected.removeAll(taskDefIdsToRemove);

        // Process
        // Perform same action 2 times, with different modifiers
        taskDefinitionViewService.removeTaskDefinitionsFromView(
                taskDefIdsToRemove,
                viewCreated.getId(),
                2L
        );
        taskDefinitionViewService.removeTaskDefinitionsFromView(
                taskDefIdsToRemove,
                viewCreated.getId(),
                3L
        );
        // remove with non-relevant task definition ids will not work, but no exception shall be thrown
        taskDefinitionViewService.removeTaskDefinitionsFromView(
                Sets.newHashSet(-1L, 12345L),
                viewCreated.getId(),
                4L
        );
        TaskDefinitionView viewAfterModify = taskDefinitionViewService.fetchById(viewCreated.getId())
                .orElseThrow(NullPointerException::new);

        // Validate
        assertThat(viewAfterModify.getIncludedTaskDefinitions().size(), is(taskDefIdsAtInit.size() - taskDefIdsToRemove.size()));
        assertThat(
                viewAfterModify.getIncludedTaskDefinitions().stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                sameBeanAs(taskDefIdsRemainingExpected)
        );
        assertThat(viewAfterModify.getLastModifier(), is(4L));
    }

    @Test
    public void overwriteTaskDefinitionsOfView_withExistingTaskDefsAndView_shouldWorkProperly() {
        // Prepare
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        List<Long> taskDefIdsAtInit = Lists.newArrayList(
                mockTaskDefs.get(0).getDefinitionId(),
                mockTaskDefs.get(1).getDefinitionId(),
                mockTaskDefs.get(2).getDefinitionId()
        );
        List<Long> taskDefIdsOverwrite = Lists.newArrayList(
                mockTaskDefs.get(2).getDefinitionId(),
                mockTaskDefs.get(3).getDefinitionId(),
                mockTaskDefs.get(4).getDefinitionId()
        );
        TaskDefinitionView viewCreated = createSimpleMockViewAndReturn(taskDefIdsAtInit);

        // Process
        TaskDefinitionView viewBeforeModify = taskDefinitionViewService.fetchById(viewCreated.getId())
                .orElseThrow(NullPointerException::new);
        taskDefinitionViewService.overwriteTaskDefinitionsOfView(
                new HashSet<>(taskDefIdsOverwrite),
                viewCreated.getId(),
                2L
        );
        TaskDefinitionView viewAfterModify = taskDefinitionViewService.fetchById(viewCreated.getId())
                .orElseThrow(NullPointerException::new);

        // Validate
        assertThat(
                viewBeforeModify.getIncludedTaskDefinitions().stream()
                        .map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                sameBeanAs(new HashSet<>(taskDefIdsAtInit))
        );
        assertThat(viewBeforeModify.getLastModifier(), is(1L));
        assertThat(
                viewAfterModify.getIncludedTaskDefinitions().stream()
                        .map(TaskDefinition::getDefinitionId).collect(Collectors.toSet()),
                sameBeanAs(new HashSet<>(taskDefIdsOverwrite))
        );
        assertThat(viewAfterModify.getLastModifier(), is(2L));
    }

    @Test
    public void overwriteTaskDefinitionsOfView_withInvalidTaskDefsOrView_shouldThrowException() {
        // case 1: view not exists
        // Prepare
        List<TaskDefinition> mockTaskDefs = createMockTaskDefsAndReturn();
        // Process
        try {
            taskDefinitionViewService.overwriteTaskDefinitionsOfView(
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
            taskDefinitionViewService.overwriteTaskDefinitionsOfView(
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

    private List<TaskDefinitionView> prepareListOfViews(int num) {
        List<TaskDefinitionView> views = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            TaskDefinitionView view = MockTaskDefinitionViewFactory.createTaskDefView(i + 1000L)
                    .cloneBuilder()
                    .withName("example_view_" + i)
                    .build();
            taskDefinitionViewService.save(view);
            views.add(view);
        }
        return views;
    }

    @Test
    public void searchPage_withValidPageConfigurations_shouldWorkProperly() {
        // prepare 100 views
        prepareListOfViews(101);
        TaskDefinitionViewSearchParams searchParamsPage1 = TaskDefinitionViewSearchParams.builder()
                .keyword(null)
                .pageNum(1)
                .pageSize(60)
                .build();
        TaskDefinitionViewSearchParams searchParamsPage2 = TaskDefinitionViewSearchParams.builder()
                .keyword(null)
                .pageNum(2)
                .pageSize(60)
                .build();
        TaskDefinitionViewSearchParams searchParamsPage3 = TaskDefinitionViewSearchParams.builder()
                .keyword(null)
                .pageNum(3)
                .pageSize(60)
                .build();
        TaskDefinitionViewSearchParams searchParamsPageExtraLarge = TaskDefinitionViewSearchParams.builder()
                .keyword(null)
                .pageNum(1)
                .pageSize(100000)
                .build();

        // Process
        PageResult<TaskDefinitionViewVO> viewVOsPage1 = taskDefinitionViewService.searchPage(searchParamsPage1);
        PageResult<TaskDefinitionViewVO> viewVOsPage2 = taskDefinitionViewService.searchPage(searchParamsPage2);
        PageResult<TaskDefinitionViewVO> viewVOsPage3 = taskDefinitionViewService.searchPage(searchParamsPage3);
        PageResult<TaskDefinitionViewVO> viewVOWithExceededSearchPageSize =  taskDefinitionViewService.searchPage(searchParamsPageExtraLarge);

        // Validate
        assertThat(viewVOsPage1.getRecords().size(), is(60));
        assertThat(viewVOsPage2.getRecords().size(), is(41));
        assertThat(viewVOsPage3.getRecords().size(), is(0));
        assertThat(viewVOWithExceededSearchPageSize.getRecords().size(), is(100));

        assertTrue(Objects.equals(viewVOsPage1.getPageNumber(), 1) &&
                Objects.equals(viewVOsPage2.getPageNumber(), 2) &&
                Objects.equals(viewVOsPage3.getPageNumber(), 3));
        assertTrue(Objects.equals(viewVOsPage1.getPageSize(), 60) &&
                Objects.equals(viewVOsPage2.getPageSize(), 60) &&
                Objects.equals(viewVOsPage3.getPageSize(), 60));
        // extra large page size should be
        assertThat(viewVOWithExceededSearchPageSize.getPageSize(), is(100));
        assertTrue(Objects.equals(viewVOsPage1.getTotalCount(), 101) &&
                Objects.equals(viewVOsPage2.getTotalCount(), 101) &&
                Objects.equals(viewVOsPage3.getTotalCount(), 101) &&
                Objects.equals(viewVOWithExceededSearchPageSize.getTotalCount(), 101));

        // there should be no duplication on records
        Set<Long> idsPage1 = viewVOsPage1.getRecords().stream().map(TaskDefinitionViewVO::getId).collect(Collectors.toSet());
        Set<Long> idsPage2 = viewVOsPage2.getRecords().stream().map(TaskDefinitionViewVO::getId).collect(Collectors.toSet());
        assertThat(idsPage1.size(), is(60));
        assertThat(idsPage2.size(), is(41));

        idsPage1.retainAll(idsPage2);
        // The intersection should be empty
        assertThat(idsPage1.size(), is(0));
    }

    @Test
    public void searchPage_withNonEmptyKeywordAsFilter_shouldDoFiltering() {
        // prepare 100 views
        prepareListOfViews(100);
        TaskDefinitionViewSearchParams searchParamsWithKeyword = TaskDefinitionViewSearchParams.builder()
                .keyword("view_1")
                .pageNum(1)
                .pageSize(100)
                .build();
        // Process
        PageResult<TaskDefinitionViewVO> searchResultPage = taskDefinitionViewService.searchPage(searchParamsWithKeyword);
        // Validate
        // expected matches: example_task_1, example_task_10, example_task_11, ..., example_task_19
        assertThat(searchResultPage.getRecords().size(), is(11));
        assertThat(searchResultPage.getTotalCount(), is(11));
    }

    @Test
    public void searchPage_withInvalidArguments_shouldThrowException() {
        // prepare 100 views
        prepareListOfViews(100);

        assertSearchWithParamsShouldThrowException("", 0, 100);
        assertSearchWithParamsShouldThrowException("", -1, 100);
        assertSearchWithParamsShouldThrowException("", 1, -1);
        assertSearchWithParamsShouldThrowException("", 1, null);
        assertSearchWithParamsShouldThrowException("", null, 100);
    }

    private void assertSearchWithParamsShouldThrowException(String keyword, Integer pageNum, Integer pageSize) {
        try {
            taskDefinitionViewService.searchPage(TaskDefinitionViewSearchParams.builder()
                .keyword(keyword)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build()
            );
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }
}
