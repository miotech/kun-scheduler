package com.miotech.kun.dataplatform.common.taskdefview.service;

import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefview.dao.TaskDefinitionViewDao;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewCreateInfoVO;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        return this.taskDefinitionViewDao.fetchById(taskDefViewId);
    }

    /**
     * Search and fetch paginated list of meta information of task definition views,
     * where task definitions are transformed into definition ids only
     * @param searchParams search parameter object
     * @return page result list of task definition view value objects
     */
    public PageResult<TaskDefinitionViewVO> searchTaskDefinitionViewPage(TaskDefinitionViewSearchParams searchParams) {
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
        return this.taskDefinitionViewDao.deleteById(id);
    }
}
