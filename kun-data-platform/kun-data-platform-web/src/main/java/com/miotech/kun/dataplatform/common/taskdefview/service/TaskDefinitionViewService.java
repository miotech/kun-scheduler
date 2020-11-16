package com.miotech.kun.dataplatform.common.taskdefview.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefview.dao.TaskDefinitionViewDao;
import com.miotech.kun.dataplatform.common.taskdefview.vo.*;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.security.service.BaseSecurityService;
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
public class TaskDefinitionViewService extends BaseSecurityService {
    private final TaskDefinitionDao taskDefinitionDao;

    private final TaskDefinitionViewDao taskDefinitionViewDao;

    private static final int PAGE_SIZE_SEARCH_MAX = 100;

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
    public Optional<TaskDefinitionView> fetchById(Long taskDefViewId) {
        Preconditions.checkNotNull(taskDefViewId);
        return this.taskDefinitionViewDao.fetchById(taskDefViewId);
    }

    /**
     * Search and fetch paginated list of meta information of task definition views,
     * where task definitions are transformed into definition ids only.
     * Maximum page size will not exceed {@code PAGE_SIZE_SEARCH_MAX} or it will be auto adjusted to the upper limit.
     * @param searchParams search parameter object
     * @return page result list of task definition view value objects
     */
    @Transactional(readOnly = true)
    public PageResult<TaskDefinitionViewVO> searchPage(TaskDefinitionViewSearchParams searchParams) {
        Preconditions.checkNotNull(searchParams);
        Preconditions.checkArgument(Objects.nonNull(searchParams.getPageNum()) && searchParams.getPageNum() >= 1);
        Preconditions.checkArgument(Objects.nonNull(searchParams.getPageSize()) && searchParams.getPageSize() >= 0);

        TaskDefinitionViewSearchParams finalParams = searchParams;
        if (searchParams.getPageSize() > PAGE_SIZE_SEARCH_MAX) {
            finalParams = searchParams.toBuilder().pageSize(PAGE_SIZE_SEARCH_MAX).build();
        }

        List<TaskDefinitionViewVO> viewList = this.taskDefinitionViewDao.fetchListBySearchParams(finalParams)
                .stream()
                .map(TaskDefinitionViewVO::from)
                .collect(Collectors.toList());
        Integer totalCount = this.taskDefinitionViewDao.fetchTotalCount(searchParams);
        return new PageResult<>(
                finalParams.getPageSize(),
                finalParams.getPageNum(),
                totalCount,
                viewList
        );
    }

    @Transactional
    public TaskDefinitionView create(CreateTaskDefViewRequest createRequest) {
        Preconditions.checkNotNull(createRequest);
        TaskDefinitionViewCreateInfoVO createInfoVO = TaskDefinitionViewCreateInfoVO.builder()
                .name(createRequest.getName())
                .creator(getCurrentUser().getId())
                .includedTaskDefinitionIds(createRequest.getTaskDefIds())
                .build();
        return create(createInfoVO);
    }

    /**
     * Create a task definition view by value object
     * @param viewCreateInfoVO info value object of the creating view
     * @return created view model object
     */
    @Transactional
    public TaskDefinitionView create(TaskDefinitionViewCreateInfoVO viewCreateInfoVO) {
        // Precondition checks:
        // 1. arguments not null
        Preconditions.checkNotNull(viewCreateInfoVO);
        // 2. included task definitions exists
        Set<Long> idSet = new HashSet<>(viewCreateInfoVO.getIncludedTaskDefinitionIds());
        assureAllTaskDefinitionsExists(idSet);

        List<TaskDefinition> taskDefinitions = taskDefinitionDao.fetchByIds(viewCreateInfoVO.getIncludedTaskDefinitionIds());
        OffsetDateTime currentTime = DateTimeUtils.now();
        TaskDefinitionView viewModelToCreate = TaskDefinitionView.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName(viewCreateInfoVO.getName())
                .withCreator(viewCreateInfoVO.getCreator())
                .withLastModifier(viewCreateInfoVO.getCreator())
                .withCreateTime(currentTime)
                .withUpdateTime(currentTime)
                .withIncludedTaskDefinitions(taskDefinitions)
                .build();
        log.debug("Creating task definition view with id = {}, name = {}", viewModelToCreate.getId(), viewModelToCreate.getName());
        return this.taskDefinitionViewDao.create(viewModelToCreate);
    }

    /**
     * Update a task definition view by request value object
     * @param viewId id of target view
     * @param request request body data
     * @return Updated task definition view model object
     */
    @Transactional
    public TaskDefinitionView update(Long viewId, UpdateTaskDefViewRequest request) {
        return update(viewId, request, getCurrentUser().getId());
    }

    @Transactional
    public TaskDefinitionView update(Long viewId, UpdateTaskDefViewRequest request, Long modifier) {
        Optional<TaskDefinitionView> fetchedView = fetchById(viewId);
        if (!fetchedView.isPresent()) {
            throw new IllegalArgumentException(String.format("Cannot update non-exist task definition view with id = %s", viewId));
        }
        OffsetDateTime currentTime = DateTimeUtils.now();
        TaskDefinitionView viewModelUpdate = fetchedView.get().cloneBuilder()
                .withName(request.getName())
                .withUpdateTime(currentTime)
                .withLastModifier(modifier)
                .withIncludedTaskDefinitions(
                        request.getTaskDefinitionIds().stream()
                                .map(id -> TaskDefinition.newBuilder()
                                        .withDefinitionId(id)
                                        .build()
                                ).collect(Collectors.toList())
                )
                .build();
        taskDefinitionViewDao.update(viewModelUpdate);
        return this.fetchById(viewId).orElseThrow(IllegalStateException::new);
    }

    @Transactional
    public TaskDefinitionView save(TaskDefinitionView viewModel) {
        return save(viewModel, false);
    }

    /**
     * Update or insert task definition view model into storage
     * @param viewModel view model object
     * @return persisted view model object
     */
    @Transactional
    public TaskDefinitionView save(TaskDefinitionView viewModel, boolean useCurrentUserAsModifier) {
        Preconditions.checkNotNull(viewModel);

        Optional<TaskDefinitionView> viewOptionalBeforeSave = this.fetchById(viewModel.getId());
        Long updatedId;
        Long modifierId = useCurrentUserAsModifier ? getCurrentUser().getId() : viewModel.getLastModifier();
        if (viewOptionalBeforeSave.isPresent()) {
            log.debug("Saving existing task definition view model with id = {}, name = {}", viewModel.getId(), viewModel.getName());
            // do update
            updatedId = viewOptionalBeforeSave.get().getId();
            taskDefinitionViewDao.update(viewModel.cloneBuilder()
                    .withLastModifier(modifierId)
                    .build()
            );
        } else {
            // do create
            log.debug("Saving non-existing task definition view model with id = {}, name = {}", viewModel.getId(), viewModel.getName());
            updatedId = Objects.isNull(viewModel.getId()) ? IdGenerator.getInstance().nextId() : viewModel.getId();
            TaskDefinitionView viewModelToCreate = viewModel.cloneBuilder()
                    .withId(updatedId)
                    .withLastModifier(modifierId)
                    .build();
            taskDefinitionViewDao.create(viewModelToCreate);
        }
        return this.fetchById(updatedId)
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Delete a task definition view by id
     * @param id task definition view id
     * @return true if found and removed successfully. false if target view not found.
     */
    @Transactional
    public boolean deleteById(Long id) {
        Preconditions.checkNotNull(id);
        return this.taskDefinitionViewDao.deleteById(id);
    }

    @Transactional
    public TaskDefinitionView putTaskDefinitionsIntoView(Set<Long> taskDefinitionIds, Long viewId) {
        return putTaskDefinitionsIntoView(taskDefinitionIds, viewId, getCurrentUser().getId());
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
        // Precondition checks:
        // 1. arguments not null
        // 2. view exists
        TaskDefinitionView view = checkArgumentsAndFetchTargetView(taskDefinitionIds, viewId, modifierId);
        // 3. task definitions exists
        assureAllTaskDefinitionsExists(taskDefinitionIds);

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
        return this.save(updatedView);
    }

    @Transactional
    public TaskDefinitionView removeTaskDefinitionsFromView(Set<Long> taskDefinitionIdsToRemove, Long viewId) {
        return removeTaskDefinitionsFromView(taskDefinitionIdsToRemove, viewId, getCurrentUser().getId());
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

        // Filter out task definitions to be removed from view by ids
        List<TaskDefinition> updatedContainingTaskDefinitions = existingTaskDefinitions
                        .stream()
                        .filter(taskDef -> !taskDefinitionIdsToRemove.contains(taskDef.getDefinitionId()))
                        .collect(Collectors.toList());

        TaskDefinitionView updatedView = view.cloneBuilder()
                .withLastModifier(modifierId)
                .withIncludedTaskDefinitions(updatedContainingTaskDefinitions)
                .build();
        return this.save(updatedView);
    }

    @Transactional
    public TaskDefinitionView overwriteTaskDefinitionsOfView(Set<Long> taskDefinitionIds, Long viewId) {
        return overwriteTaskDefinitionsOfView(taskDefinitionIds, viewId, getCurrentUser().getId());
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
        // Precondition checks:
        // 1. arguments not null
        // 2. view exists
        TaskDefinitionView view = checkArgumentsAndFetchTargetView(taskDefinitionIds, viewId, modifierId);
        // 3. task definitions exists
        assureAllTaskDefinitionsExists(taskDefinitionIds);

        TaskDefinitionView updatedView = view.cloneBuilder()
                .withLastModifier(modifierId)
                .withIncludedTaskDefinitions(taskDefinitionDao.fetchByIds(new ArrayList<>(taskDefinitionIds)))
                .build();
        return this.save(updatedView);
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

    private void assureAllTaskDefinitionsExists(Set<Long> taskDefinitionIds) {
        Set<Long> idSet = new HashSet<>(taskDefinitionIds);
        Set<Long> fetchedTaskDefIdSet = taskDefinitionDao.fetchByIds(
                taskDefinitionIds.stream().collect(Collectors.toList())
        ).stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toSet());

        if (!Objects.equals(idSet, fetchedTaskDefIdSet)) {
            idSet.removeAll(fetchedTaskDefIdSet);
            throw new IllegalArgumentException(
                    String.format("Trying to modify view with non-existing task definition ids: %s into view.",
                            StringUtils.join(idSet.stream().map(String::valueOf).collect(Collectors.toList()), ",")
                    ));
        }
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
