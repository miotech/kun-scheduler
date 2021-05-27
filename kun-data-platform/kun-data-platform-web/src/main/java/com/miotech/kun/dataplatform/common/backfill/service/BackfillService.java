package com.miotech.kun.dataplatform.common.backfill.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.common.backfill.dao.BackfillDao;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillCreateInfo;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillSearchParams;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.RunTaskRequest;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.dataplatform.constant.BackfillConstants.MAX_BACKFILL_TASKS;

@Service
@Slf4j
public class BackfillService extends BaseSecurityService {
    @Autowired
    private BackfillDao backfillDao;

    @Autowired
    private WorkflowClient workflowClient;

    /** 按照 id 查询 Backfill instance */
    public Optional<Backfill> fetchById(Long backfillId) {
        Preconditions.checkNotNull(backfillId, "Argument `backfillId` should not be null.");
        return backfillDao.fetchById(backfillId);
    }

    /** 分页搜索 backfill */
    public PageResult<Backfill> search(BackfillSearchParams searchParams) {
        Preconditions.checkNotNull(searchParams, "search parameters cannot be null");
        if (searchParams.getPageSize() != null && searchParams.getPageSize() > 100) {
            log.debug("Large page size detected. Adjusting page size to 100");
            searchParams.setPageSize(100);
        }
        return backfillDao.search(searchParams);
    }

    /** 创建一个 backfill 并立即执行 */
    public Backfill createAndRun(BackfillCreateInfo createInfo) {
        // 单次 backfill 不允许每次执行超过 MAX_BACKFILL_TASKS (目前为 100) 个 task。原因：
        // (1) 查询 taskRuns 需要多次请求 workflow API，效率低
        // (2) 过于大批量的执行任务不适用于 Backfill 的场景，容易造成资源紧张
        // (3) 防止恶意调用
        Preconditions.checkArgument(
                createInfo.getWorkflowTaskIds().size() <= MAX_BACKFILL_TASKS,
                String.format("Cannot run more than %d tasks in a single backfill batch at once", MAX_BACKFILL_TASKS)
        );

        UserInfo userInfo = getCurrentUser();
        if (Objects.isNull(userInfo)) {
            throw new IllegalStateException("Cannot get information of current user.");
        }
        OffsetDateTime now = DateTimeUtils.now();
        List<Long> workflowTaskIds = createInfo.getWorkflowTaskIds();
        // Submit run request to workflow module by invoking API
        Map<Long, TaskRun> taskIdToTaskRunMap = runWorkflowTasks(createInfo.getWorkflowTaskIds());
        // Get created task runs
        List<Long> taskRunIds = workflowTaskIds.stream()
                .map(taskId -> {
                    TaskRun taskRun = taskIdToTaskRunMap.get(taskId);
                    if (taskRun == null) {
                        throw new IllegalStateException(String.format("Cannot get corresponding task run entity of task id: %s", taskId));
                    }
                    // else
                    return taskRun.getId();
                })
                .collect(Collectors.toList());

        Backfill backfillToCreate = Backfill.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName(createInfo.getName())
                .withCreator(getCurrentUser().getId())
                .withTaskRunIds(taskRunIds)
                .withWorkflowTaskIds(createInfo.getWorkflowTaskIds())
                .withTaskDefinitionIds(createInfo.getTaskDefinitionIds())
                .withCreateTime(now)
                .withUpdateTime(now)
                .build();
        log.debug("Creating backfill with id = {}, name = {}, task definition ids = {}, task ids = {}",
                backfillToCreate.getId(),
                backfillToCreate.getName(),
                backfillToCreate.getTaskDefinitionIds(),
                backfillToCreate.getWorkflowTaskIds());

        return backfillDao.create(backfillToCreate);
    }

    /** 按 id 执行对应的 backfill。若成功返回 true，若 Backfill 不存在返回 false */
    public boolean runBackfillById(Long backfillId) {
        // preconditions check
        Preconditions.checkNotNull(backfillId, "Backfill id cannot be null");

        Optional<Backfill> backfillOptional = backfillDao.fetchById(backfillId);
        if (!backfillOptional.isPresent()) {
            log.debug("Cannot find target backfill with id: {}", backfillId);
            return false;
        }
        Backfill backfill = backfillOptional.get();
        Map<Long, TaskRun> taskIdToUpdatedTaskRuns = runWorkflowTasks(backfill.getWorkflowTaskIds());

        // update task run ids
        List<Long> updatedTaskRunIds = new ArrayList<>();
        for (Long taskId : backfill.getWorkflowTaskIds()) {
            TaskRun taskRun = taskIdToUpdatedTaskRuns.get(taskId);
            if (taskRun == null) {
                throw new IllegalStateException(String.format("Cannot get corresponding task run entity of task id: %s", taskId));
            }
            updatedTaskRunIds.add(taskRun.getId());
        }
        Backfill updatedBackfill = backfill.cloneBuilder()
                .withTaskRunIds(updatedTaskRunIds)
                .build();
        backfillDao.update(backfillId, updatedBackfill);
        return true;
    }

    private Map<Long, TaskRun> runWorkflowTasks(List<Long> workflowTaskIds) {
        // construct request body
        RunTaskRequest runTaskRequest = new RunTaskRequest();
        for (Long taskId : workflowTaskIds) {
            // TODO: is there any extra configuration needed?
            runTaskRequest.addTaskConfig(taskId, Maps.newHashMap());
        }
        log.debug("Executing workflow task ids: {}", workflowTaskIds);
        // send by workflow client
        return workflowClient.executeTasks(runTaskRequest);
    }

    /** 按照 id 查询 Backfill instance 实例对应的 Task run instances 列表 */
    public List<TaskRun> fetchTaskRunsByBackfillId(Long backfillId) {
        Preconditions.checkNotNull(backfillId, "Argument `backfillId` should not be null.");
        Optional<Backfill> backfillOptional = backfillDao.fetchById(backfillId);
        if (!backfillOptional.isPresent()) {
            throw new IllegalArgumentException(String.format("Cannot find backfill with id: %s", backfillId));
        }
        Backfill backfill = backfillOptional.get();

        List<TaskRun> taskRuns = new ArrayList<>(backfill.getTaskRunIds().size());
        for (Long taskRunId : backfill.getTaskRunIds()) {
            TaskRun taskRun = workflowClient.getTaskRun(taskRunId);
            taskRuns.add(taskRun);
        }
        return taskRuns;
    }

    /** 停止 id 对应的 Backfill 中的所有 TaskRuns */
    public void stopBackfillById(Long backfillId) {
        Preconditions.checkNotNull(backfillId, "Argument `backfillId` should not be null.");
        Optional<Backfill> backfillOptional = backfillDao.fetchById(backfillId);
        if (!backfillOptional.isPresent()) {
            throw new IllegalArgumentException(String.format("Cannot find backfill with id: %s", backfillId));
        }
        Backfill backfill = backfillOptional.get();

        workflowClient.stopTaskRuns(backfill.getTaskRunIds());
    }

    /**
     * Check if a task run is initialized by a backfill. If true, return id of that backfill
     * @param taskRunId id of the task run
     * @return Wrapped optional ID object if found. Else returns empty optional.
     */
    public Optional<Long> findDerivedFromBackfill(Long taskRunId) {
        return backfillDao.fetchBackfillIdByTaskRunId(taskRunId);
    }
}
