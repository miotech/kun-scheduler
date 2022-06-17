package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.bo.TaskRunProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import com.miotech.kun.workflow.core.model.common.Condition;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.*;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Singleton
public class TaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final Executor executor;

    private final TaskRunDao taskRunDao;

    private final EventBus eventBus;

    private final Props props;

    private InnerEventLoop eventLoop;

    private final Map<Long, Boolean> rerunningTaskRunIds = new ConcurrentHashMap<>();

    private final Integer EVENT_QUEUE_SIZE = 10000;

    private BlockingQueue<TaskRunReadyCheckEvent> taskRunReadyCheckEventQueue = new LinkedBlockingQueue<>(EVENT_QUEUE_SIZE);


    @Inject
    public TaskManager(Executor executor, TaskRunDao taskRunDao, EventBus eventBus, Props props) {
        this.executor = executor;
        this.taskRunDao = taskRunDao;
        this.props = props;

        this.eventLoop = new InnerEventLoop();
        eventLoop.start();

        this.eventBus = eventBus;
        this.eventBus.register(this.eventLoop);
        init();

    }

    /* ----------- public methods ------------ */

    public void submit(List<TaskRun> taskRuns) {
        // 生成对应的TaskAttempt
        List<TaskAttempt> taskAttempts = taskRuns.stream()
                .map(taskRun -> createTaskAttempt(taskRun, true)).collect(Collectors.toList());
        logger.debug("TaskAttempts saved. total={}", taskAttempts.size());
        save(taskAttempts);
        for (TaskAttempt taskAttempt : taskAttempts) {
            resolveTaskRunStatus(taskAttempt);
        }
        trigger();
    }

    /**
     * trigger runnable taskRun to start
     */
    public void trigger() {
        TaskRunReadyCheckEvent event = new TaskRunReadyCheckEvent(System.currentTimeMillis());
        taskRunReadyCheckEventQueue.offer(event);

    }

    /**
     * taskRun status must be success, failed, check_failed
     *
     * @param taskRun
     */
    public boolean retry(TaskRun taskRun) {
        checkState(taskRun.getStatus().allowRetry(), "taskRun status must be allowed to retry");
        // Does the same re-run request invoked in another threads?
        if (rerunningTaskRunIds.put(taskRun.getId(), Boolean.TRUE) != null) {
            logger.warn("Cannot rerun taskrun instance with id = {}. Reason: another thread is attempting to re-run the same task run.", taskRun.getId());
            return false;
        }
        try {
            //0. prepare task attempt
            TaskAttempt taskAttempt = createTaskAttempt(taskRun, false);
            logger.info("save rerun taskAttempt, taskAttemptId = {}, attempt = {}", taskAttempt.getId(), taskAttempt.getAttempt());
            save(Arrays.asList(taskAttempt));

            //1. post reschedule event
            TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.RESCHEDULE, taskAttempt.getId());
            eventBus.post(taskRunTransitionEvent);

            //2. fetch task run should be crated
            List<Long> taskRunShouldBeCreated = taskRun.getStatus().isFailure()?
                    updateDownStreamStatus(taskRun.getId(), TaskRunStatus.CREATED, Lists.newArrayList(TaskRunStatus.UPSTREAM_FAILED))
                    : new ArrayList<>();
            taskRunShouldBeCreated.add(taskRun.getId());

            //3. update taskrun term_at to null
            taskRunDao.resetTaskRunTimestampToNull(taskRunShouldBeCreated, "term_at");

            //4. update conditions' result which use this task run as condition to be false;
            updateTaskRunConditions(taskRunShouldBeCreated, TaskRunStatus.CREATED);

            //5. update taskruns in created which need to be blocked as blocked
            List<Long> restrictedTaskRunIds = fetchRestrictedTaskRunsToBeUpdatedIds(Collections.singletonList(taskRun.getId()));
            List<Long> taskRunIdsWithBlocked = taskRunDao.fetchTaskRunIdsWithBlockType(restrictedTaskRunIds);
            taskRunDao.updateTaskRunStatusByTaskRunId(taskRunIdsWithBlocked, TaskRunStatus.BLOCKED, Collections.singletonList(TaskRunStatus.CREATED));

            trigger();
            return true;
        } catch (Exception e) {
            logger.error("Failed to re-run taskrun with id = {} due to exceptions.", taskRun.getId());
            throw e;
        } finally {
            // release the lock
            rerunningTaskRunIds.remove(taskRun.getId());
        }
    }

    public boolean skip(TaskRun taskRun) {
        //0.check state of taskrun
        checkState(taskRun.getStatus().isFailure(), "This taskRun is not allowed to skip. Status not match");
        //1. reuse latest task attempt
        TaskAttemptProps taskAttempt = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        //2. post skip taskrun transition event
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.SKIP, taskAttempt.getId());
        eventBus.post(taskRunTransitionEvent);

        //3. taskrun status changed to skipped, update condition table row which condition is this taskrun
        updateTaskRunConditions(Collections.singletonList(taskRun.getId()), TaskRunStatus.SKIPPED);

        //4. for taskrun is failure, update it's downstream which are upstream_failed
        if (taskRun.getStatus().isFailure()) {
            List<Long> taskRunShouldBeCreated = updateDownStreamStatus(taskRun.getId(), TaskRunStatus.CREATED, Lists.newArrayList(TaskRunStatus.UPSTREAM_FAILED));
            taskRunDao.resetTaskRunTimestampToNull(taskRunShouldBeCreated, "term_at");
            updateTaskRunConditions(taskRunShouldBeCreated, TaskRunStatus.CREATED);
            logger.info("taskrun should be created {}", taskRunShouldBeCreated);
            updateRestrictedTaskRunsStatus(taskRunShouldBeCreated);
        }

        trigger();
        return true;
    }

    public boolean removeDependency(Long taskRunId, List<Long> upstreamTaskRunIdsToRemoveDependency) {
        TaskRun taskRun = taskRunDao.fetchTaskRunById(taskRunId).orElse(null);
        if (taskRun == null) {
            return false;
        }
        checkState(taskRun.getStatus().allowRemoveDependency(), "This taskrun is not allowed to remove dependency. Status not match");

        List<TaskRun> upstreamTaskRunsToRemoveDependency = taskRunDao.fetchTaskRunsByIds(upstreamTaskRunIdsToRemoveDependency).stream()
                .map(r -> r.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        //remove dependency in database
        taskRunDao.removeTaskRunDependency(taskRunId, upstreamTaskRunIdsToRemoveDependency);

        //reschedule taskRun if necessary
        TaskAttemptProps taskAttempt = taskRunDao.fetchLatestTaskAttempt(taskRunId);

        if (taskAttempt.getStatus().isUpstreamFailed()) {
            //fetch remained failed upstream task run ids
            List<TaskRun> remainedUpstreamTaskRuns = taskRunDao.fetchUpstreamTaskRunsById(taskRunId, 1, false);
            List<Long> remainedFailedUpstreamTaskRunIds = fetchFailedUpstreamTaskRunIdCollectionFromTaskRuns(remainedUpstreamTaskRuns);

            //fetch failed upstream taskrun ids to be removed
            List<Long> failedUpstreamTaskRunIdsToBeRemoved = fetchFailedUpstreamTaskRunIdCollectionFromTaskRuns(upstreamTaskRunsToRemoveDependency);

            //get intersects
            failedUpstreamTaskRunIdsToBeRemoved.removeAll(remainedFailedUpstreamTaskRunIds);

            // no need to update downstream, nothing changed
            if (failedUpstreamTaskRunIdsToBeRemoved.isEmpty()) {
                return true;
            }

            //update failed upstream task run ids
            List<Long> taskRunShouldUpdateFailedUpstreamTaskRunIds = new ArrayList<>();
            taskRunShouldUpdateFailedUpstreamTaskRunIds.addAll(taskRunDao.fetchDownStreamTaskRunIdsRecursive(taskRunId));
            taskRunShouldUpdateFailedUpstreamTaskRunIds.add(taskRunId);
            taskRunDao.removeFailedUpstreamTaskRunIds(taskRunShouldUpdateFailedUpstreamTaskRunIds, failedUpstreamTaskRunIdsToBeRemoved);

            //fetch and update upstream_failed task run which should be created
            List<Long> taskRunShouldBeCreated = new ArrayList<>(taskRunDao.taskRunShouldBeCreated(taskRunShouldUpdateFailedUpstreamTaskRunIds));
            taskRunDao.updateAttemptStatusByTaskRunIds(taskRunShouldBeCreated, TaskRunStatus.CREATED, null, Lists.newArrayList(TaskRunStatus.UPSTREAM_FAILED));
            //update taskrun term_at to null
            taskRunDao.resetTaskRunTimestampToNull(taskRunShouldBeCreated, "term_at");
            //update conditions' result which use this task run as condition to be false;
            updateTaskRunConditions(taskRunShouldBeCreated, TaskRunStatus.CREATED);
            //update taskruns in created which need to be blocked as blocked
            List<Long> restrictedTaskRunIds = fetchRestrictedTaskRunsToBeUpdatedIds(taskRunShouldBeCreated);
            List<Long> taskRunIdsWithBlocked = taskRunDao.fetchTaskRunIdsWithBlockType(restrictedTaskRunIds);
            taskRunDao.updateTaskRunStatusByTaskRunId(taskRunIdsWithBlocked, TaskRunStatus.BLOCKED, Collections.singletonList(TaskRunStatus.CREATED));
        }

        //trigger runnable taskRun
        trigger();
        return true;
    }

    /* ----------- private methods ------------ */

    private void init() {
        Thread readyEventConsumer = new Thread(new TaskReadyEventConsumer());
        readyEventConsumer.start();
    }

    private TaskAttempt createTaskAttempt(TaskRun taskRun, boolean reUseLatest) {
        checkNotNull(taskRun, "taskRun should not be null.");
        checkNotNull(taskRun.getId(), "taskRun's id should not be null.");

        TaskAttemptProps savedTaskAttempt = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        int attempt = 1;
        if (savedTaskAttempt != null) {
            attempt = savedTaskAttempt.getAttempt() + 1;
        } else {
            reUseLatest = false;
        }
        TaskAttempt taskAttempt = TaskAttempt.newBuilder()
                .withId(reUseLatest ? savedTaskAttempt.getId() : WorkflowIdGenerator.nextTaskAttemptId(taskRun.getId(), attempt))
                .withTaskRun(taskRun)
                .withAttempt(reUseLatest ? savedTaskAttempt.getAttempt() : attempt)
                .withStatus(reUseLatest ? savedTaskAttempt.getStatus() : determineInitStatus(taskRun))
                .withQueueName(taskRun.getQueueName())
                .withPriority(taskRun.getPriority())
                .withRetryTimes(0)
                .withExecutorLabel(taskRun.getExecutorLabel())
                .build();
        logger.debug("Created taskAttempt. taskAttemptId = {}", taskAttempt.getId());

        return taskAttempt;
    }

    private TaskRunStatus determineInitStatus(TaskRun taskRun) {
        return detectUpstreamFailedOrBlocked(taskRun) ? taskRun.getStatus() : TaskRunStatus.CREATED;
    }

    private boolean detectUpstreamFailedOrBlocked(TaskRun taskRun) {
        return taskRun.getStatus().equals(TaskRunStatus.UPSTREAM_FAILED) ||
                taskRun.getStatus().equals(TaskRunStatus.BLOCKED);
    }

    private void save(List<TaskAttempt> taskAttempts) {
        for (TaskAttempt ta : taskAttempts) {
            taskRunDao.createAttempt(ta);
        }
    }

    private void resolveTaskRunStatus(TaskAttempt taskAttempt) {
        TaskRun taskRun = taskAttempt.getTaskRun();
        List<TaskRunProps> upstreamTaskRuns = taskRunDao.fetchUpstreamTaskRunsById(taskRun.getId());
        Task task = taskRun.getTask();
        List<TaskRunCondition> taskRunConditions = taskRun.getTaskRunConditions();
        if (task.getDependencies().size() > upstreamTaskRuns.size()) {
            logger.error("dependency not satisfy, taskId = {}", task.getId());
            return;
        }

        // UPSTREAM_FAILED is given higher priority than BLOCKED
        // TaskRun's status can be both UPSTREAM_FAILED and BLOCKED, UPSTREAM_FAILED is set in such situation

        //check upstream exists failed or upstream_failed
        boolean allUpstreamSuccess = true;
        for (TaskRunProps upstreamTaskRun : upstreamTaskRuns) {
            if (upstreamTaskRun.getStatus().isFailure()
                    || upstreamTaskRun.getStatus().equals(TaskRunStatus.UPSTREAM_FAILED)) {
                TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.UPSTREAM_FAILED, taskAttempt.getId());
                eventBus.post(taskRunTransitionEvent);
            }
            if (!upstreamTaskRun.getStatus().isSuccess()) {
                allUpstreamSuccess = false;
            }
        }
        // when all upstream dependencies are satisfied, check whether is blocked
        if (allUpstreamSuccess) {
            for (TaskRunCondition taskRunCondition : taskRunConditions) {
                if (taskRunCondition.getType().equals(ConditionType.TASKRUN_PREDECESSOR_FINISH) && !taskRunCondition.getResult()) {
                    TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.HANGUP, taskAttempt.getId());
                    eventBus.post(taskRunTransitionEvent);
                }
            }
        }
    }

    private List<Long> fetchFailedUpstreamTaskRunIdCollectionFromTaskRuns(List<TaskRun> upstreamTaskRuns) {
        List<Long> failedUpstreamTaskRunIds = new ArrayList<>();
        for (TaskRun upstreamTaskRun : upstreamTaskRuns) {
            if (upstreamTaskRun.getStatus().isFailure()) {
                failedUpstreamTaskRunIds.add(upstreamTaskRun.getId());
            } else if (upstreamTaskRun.getStatus().isUpstreamFailed()) {
                failedUpstreamTaskRunIds.addAll(upstreamTaskRun.getFailedUpstreamTaskRunIds());
            }
        }
        return failedUpstreamTaskRunIds;
    }


    private class InnerEventLoop extends EventLoop<Long, Event> {
        public InnerEventLoop() {
            super("task-manager");
            addConsumers(Arrays.asList(new StatusChangeEventConsumer()));
        }

        @Subscribe
        public void onReceive(TaskAttemptStatusChangeEvent event) {
            post(event.getAttemptId(), event);
        }

    }

    private class StatusChangeEventConsumer extends EventConsumer<Long, Event> {
        @Override
        public void onReceive(Event event) {
            if (event instanceof TaskAttemptStatusChangeEvent) {
                TaskAttemptStatusChangeEvent taskAttemptStatusChangeEvent = (TaskAttemptStatusChangeEvent) event;
                Long taskRunId = taskAttemptStatusChangeEvent.getTaskRunId();
                TaskRunStatus currentStatus = taskAttemptStatusChangeEvent.getToStatus();
                List<Long> ids = new ArrayList<>(Collections.singletonList(taskRunId));
                if (currentStatus.isFinished()) {
                    updateTaskRunConditions(ids, currentStatus);
                    List<Long> downstreamTaskRunIds = null;
                    if (currentStatus.isFailure()) {
                        downstreamTaskRunIds = updateDownStreamStatus(taskRunId, TaskRunStatus.UPSTREAM_FAILED, Lists.newArrayList(TaskRunStatus.CREATED));
                        updateTaskRunConditions(downstreamTaskRunIds, TaskRunStatus.UPSTREAM_FAILED);
                        ids.addAll(downstreamTaskRunIds);
                    }
                    updateRestrictedTaskRunsStatus(ids);
                    trigger();
                }
            }
        }
    }

    private void updateTaskRunConditions(List<Long> taskRunIds, TaskRunStatus status) {
        if (taskRunIds.isEmpty()) return;
        taskRunDao.updateConditionsWithTaskRuns(taskRunIds, status);
    }

    private void updateRestrictedTaskRunsStatus(List<Long> taskRunIds) {
        if (taskRunIds.isEmpty()) {
            return;
        }

        List<Long> restrictedTaskRunIds = fetchRestrictedTaskRunsToBeUpdatedIds(taskRunIds);

        List<TaskRunStatus> allowToUpdateStatus = Arrays.asList(TaskRunStatus.CREATED, TaskRunStatus.BLOCKED);

        List<Long> taskRunIdsWithBlocked = taskRunDao.fetchTaskRunIdsWithBlockType(restrictedTaskRunIds);
        taskRunDao.updateTaskRunStatusByTaskRunId(taskRunIdsWithBlocked, TaskRunStatus.BLOCKED, allowToUpdateStatus);

        restrictedTaskRunIds.removeAll(taskRunIdsWithBlocked);
        taskRunDao.updateTaskRunStatusByTaskRunId(restrictedTaskRunIds, TaskRunStatus.CREATED, allowToUpdateStatus);
    }

    private List<Long> fetchRestrictedTaskRunsToBeUpdatedIds(List<Long> taskRunIds) {
        List<Condition> conditions = taskRunIds.stream()
                .map(x -> new Condition(Collections.singletonMap("taskRunId", x.toString())))
                .collect(Collectors.toList());
        List<Long> restrictedTaskRunIds = taskRunDao.fetchRestrictedTaskRunIdsFromConditions(conditions);

        restrictedTaskRunIds.removeAll(taskRunIds);

        List<Long> taskRunIdsNotProcess = taskRunDao.fetchRestrictedTaskRunIdsWithConditionType(restrictedTaskRunIds, ConditionType.TASKRUN_DEPENDENCY_SUCCESS);
        restrictedTaskRunIds.removeAll(taskRunIdsNotProcess);

        return restrictedTaskRunIds;
    }

    private void submitSatisfyTaskAttemptToExecutor() {
        List<TaskAttempt> taskAttemptList = taskRunDao.fetchAllSatisfyTaskAttempt();
        logger.debug("fetch satisfy taskAttempt size = {}", taskAttemptList.size());
        for (TaskAttempt taskAttempt : taskAttemptList) {
            try {
                executor.submit(taskAttempt);
            } catch (Exception e) {
                logger.warn("submit taskAttempt = {} to executor failed", taskAttempt.getId(), e);
            }
        }
    }

    /**
     * update taskRunId's downstream taskrun to taskRunStatus which is in filterStatus
     * @param taskRunId
     * @param taskRunStatus
     * @param filterStatus
     * @return
     */
    private List<Long> updateDownStreamStatus(Long taskRunId, TaskRunStatus taskRunStatus, List<TaskRunStatus> filterStatus) {
        //fetch all downstream task runs
        List<Long> downstreamTaskRunIds = taskRunDao.fetchDownStreamTaskRunIdsRecursive(taskRunId);
        logger.debug("fetch downStream taskRunIds = {},taskRunId = {}", downstreamTaskRunIds, taskRunId);

        //update downstream taskrun "failed_upstream_task_run_id" field with taskRunId by taskRunStatus
        taskRunDao.updateTaskRunWithFailedUpstream(taskRunId, downstreamTaskRunIds, taskRunStatus);

        if (taskRunStatus.isTermState()) {
            OffsetDateTime termAt = DateTimeUtils.now();
            taskRunDao.updateAttemptStatusByTaskRunIds(downstreamTaskRunIds, taskRunStatus, termAt, filterStatus);
            return downstreamTaskRunIds;
        } else {
            //fetch taskRun should update to created by dependency
            List<Long> taskRunShouldBeCreated = taskRunDao.taskRunShouldBeCreated(downstreamTaskRunIds);
            taskRunDao.updateAttemptStatusByTaskRunIds(taskRunShouldBeCreated, taskRunStatus, null, filterStatus);
            return taskRunShouldBeCreated;
        }

    }


    private class TaskRunReadyCheckEvent {

        private final long timestamp;

        TaskRunReadyCheckEvent(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private class TaskReadyEventConsumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    List<TaskRunReadyCheckEvent> eventList = new ArrayList<>();
                    taskRunReadyCheckEventQueue.take();
                    taskRunReadyCheckEventQueue.drainTo(eventList);
                    submitSatisfyTaskAttemptToExecutor();
                } catch (Throwable e) {
                    logger.warn("take taskRun ready check event from queue failed");
                }
            }
        }
    }

}
