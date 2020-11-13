package com.miotech.kun.dataplatform.common.taskdefview.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefview.dao.TaskDefinitionViewDao;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewCreateInfoVO;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskDefinitionViewService {
    private final TaskDefinitionDao taskDefinitionDao;

    private final TaskDefinitionViewDao taskDefinitionViewDao;

    @Autowired
    public TaskDefinitionViewService(TaskDefinitionViewDao taskDefinitionViewDao, TaskDefinitionDao taskDefinitionDao) {
        this.taskDefinitionViewDao = taskDefinitionViewDao;
        this.taskDefinitionDao = taskDefinitionDao;
    }

    /**
     * Fetch task definition view object detail by id
     * @param taskDefViewId id of target task definition view
     * @return An optional object wrapper for task definition view model
     */
    public Optional<TaskDefinitionView> fetchTaskDefinitionViewById(Long taskDefViewId) {
        Preconditions.checkNotNull(taskDefViewId);
        return this.taskDefinitionViewDao.fetchById(taskDefViewId);
    }

    /**
     * Search and fetch paginated list of meta information of task definition views,
     * where task definitions are transformed into definition ids only
     * @param searchParams search parameter object
     * @return page result list of task definition view value objects
     */
    public PageResult<TaskDefinitionViewVO> searchTaskDefinitionViewPage(TaskDefinitionViewSearchParams searchParams) {
        Preconditions.checkNotNull(searchParams);

        List<TaskDefinitionViewVO> viewList = this.taskDefinitionViewDao.fetchListBySearchParams(searchParams)
                .stream()
                .map(TaskDefinitionViewVO::from)
                .collect(Collectors.toList());
        Integer totalCount = this.taskDefinitionViewDao.fetchTotalCount();
        return new PageResult<>(
                searchParams.getPageSize(),
                searchParams.getPageNum(),
                totalCount,
                viewList
        );
    }

    /**
     * Create a task definition view by value object
     * @param viewCreateInfoVO info value object of the creating view
     * @return created view model object
     */
    @Transactional
    public TaskDefinitionView createTaskDefinitionView(TaskDefinitionViewCreateInfoVO viewCreateInfoVO) {
        Preconditions.checkNotNull(viewCreateInfoVO);

        OffsetDateTime currentTime = DateTimeUtils.now();
        TaskDefinitionView viewModelToCreate = TaskDefinitionView.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName(viewCreateInfoVO.getName())
                .withCreator(viewCreateInfoVO.getCreator())
                .withLastModifier(viewCreateInfoVO.getCreator())
                .withCreateTime(currentTime)
                .withUpdateTime(currentTime)
                .withIncludedTaskDefinitions(
                        taskDefinitionDao.fetchByIds(viewCreateInfoVO.getIncludedTaskDefinitionIds())
                )
                .build();
        return this.taskDefinitionViewDao.create(viewModelToCreate);
    }

    /**
     * Update or insert task definition view model into storage
     * @param viewModel view model object
     * @return persisted view model object
     */
    @Transactional
    public TaskDefinitionView saveTaskDefinitionView(TaskDefinitionView viewModel) {
        Preconditions.checkNotNull(viewModel);

        Optional<TaskDefinitionView> viewOptionalBeforeSave = this.fetchTaskDefinitionViewById(viewModel.getId());
        Long updatedId;
        if (viewOptionalBeforeSave.isPresent()) {
            // do update
            updatedId = viewOptionalBeforeSave.get().getId();
            taskDefinitionViewDao.update(viewModel);
        } else {
            // do create
            updatedId = Objects.isNull(viewModel.getId()) ? IdGenerator.getInstance().nextId() : viewModel.getId();
            TaskDefinitionView viewModelToCreate = viewModel.cloneBuilder()
                    .withId(updatedId)
                    .build();
            taskDefinitionViewDao.create(viewModelToCreate);
        }
        return this.fetchTaskDefinitionViewById(updatedId)
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Delete a task definition view by id
     * @param id task definition view id
     * @return true if found and removed successfully. false if target view not found.
     */
    @Transactional
    public boolean deleteTaskDefinitionViewById(Long id) {
        Preconditions.checkNotNull(id);
        return this.taskDefinitionViewDao.deleteById(id);
    }

    /**
     * Put task definitions into view inclusive list. The result should be idempotence.
     * @param taskDefinitionIds ids of task definitions
     * @param viewId id of task definition view
     * @return Updated task definition view
     * @throws NullPointerException when view with target id not found
     */
    @Transactional
    public TaskDefinitionView putTaskDefinitionsIntoView(Set<Long> taskDefinitionIds, Long viewId, Long modifierId) {
        TaskDefinitionView view = checkArgumentsAndFetchTargetView(taskDefinitionIds, viewId, modifierId);

        List<TaskDefinition> existingTaskDefinitions = view.getIncludedTaskDefinitions();
        List<TaskDefinition> updatedContainingTaskDefinitions = loadTaskDefinitionsFromIdSets(
                existingTaskDefinitions.stream()
                        .map(TaskDefinition::getDefinitionId)
                        .collect(Collectors.toSet()),
                taskDefinitionIds
        );

        TaskDefinitionView updatedView = view.cloneBuilder()
                .withLastModifier(modifierId)
                .withIncludedTaskDefinitions(updatedContainingTaskDefinitions)
                .build();
        return this.saveTaskDefinitionView(updatedView);
    }

    /**
     * Remove task definitions from view inclusive list. The result should be idempotence.
     * @param taskDefinitionIdsToRemove ids of task definitions to remove
     * @param viewId id of task definition view
     * @return Updated task definition view
     * @throws NullPointerException when view with target id not found
     */
    @Transactional
    public TaskDefinitionView removeTaskDefinitionsFromView(Set<Long> taskDefinitionIdsToRemove, Long viewId, Long modifierId) {
        TaskDefinitionView view = checkArgumentsAndFetchTargetView(taskDefinitionIdsToRemove, viewId, modifierId);
        List<TaskDefinition> existingTaskDefinitions = view.getIncludedTaskDefinitions();

        // Filter out by ids
        List<TaskDefinition> updatedContainingTaskDefinitions = existingTaskDefinitions
                        .stream()
                        .filter(taskDef -> !taskDefinitionIdsToRemove.contains(taskDef.getDefinitionId()))
                        .collect(Collectors.toList());

        TaskDefinitionView updatedView = view.cloneBuilder()
                .withLastModifier(modifierId)
                .withIncludedTaskDefinitions(updatedContainingTaskDefinitions)
                .build();
        return this.saveTaskDefinitionView(updatedView);
    }

    /**
     * Completely reset containing task definitions of target view. The result should be idempotence.
     * @param taskDefinitionIds ids of task definitions
     * @param viewId id of task definition view
     * @return Updated task definition view
     * @throws NullPointerException when view with target id not found
     */
    @Transactional
    public TaskDefinitionView overwriteTaskDefinitionsOfView(Set<Long> taskDefinitionIds, Long viewId, Long modifierId) {
        TaskDefinitionView view = checkArgumentsAndFetchTargetView(taskDefinitionIds, viewId, modifierId);
        TaskDefinitionView updatedView = view.cloneBuilder()
                .withLastModifier(modifierId)
                .withIncludedTaskDefinitions(taskDefinitionDao.fetchByIds(new ArrayList<>(taskDefinitionIds)))
                .build();
        return this.saveTaskDefinitionView(updatedView);
    }

    private TaskDefinitionView checkArgumentsAndFetchTargetView(Set<Long> taskDefIds, Long viewId, Long modifierId) {
        Preconditions.checkNotNull(taskDefIds);
        Preconditions.checkNotNull(viewId);
        Preconditions.checkNotNull(modifierId);
        Optional<TaskDefinitionView> viewOptional = taskDefinitionViewDao.fetchById(viewId);
        if (!viewOptional.isPresent()) {
            throw new NullPointerException(String.format("Cannot find view with id = %s", viewId));
        }
        return viewOptional.get();
    }

    @SafeVarargs
    private final List<TaskDefinition> loadTaskDefinitionsFromIdSets(Set<Long>... taskDefIds) {
        Set<Long> finalTaskDefinitionIds = new HashSet<>();
        for (Set<Long> set : taskDefIds) {
            finalTaskDefinitionIds.addAll(set);
        }
        return taskDefinitionDao.fetchByIds(new ArrayList<>(finalTaskDefinitionIds));
    }
}
