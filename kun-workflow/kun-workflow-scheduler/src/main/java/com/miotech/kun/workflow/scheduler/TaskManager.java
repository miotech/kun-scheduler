package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class TaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private static final Integer EVENT_LOOP_WORKER_NUM = Runtime.getRuntime().availableProcessors() * 2;

    private final Executor executor;

    private final TaskRunDao taskRunDao;

    private final EventBus eventBus;

    private InnerEventLoop eventLoop;

    @Inject
    public TaskManager(Executor executor, TaskRunDao taskRunDao, EventBus eventBus) {
        this.executor = executor;
        this.taskRunDao = taskRunDao;

        this.eventLoop = new InnerEventLoop(EVENT_LOOP_WORKER_NUM);
        this.eventLoop.start();

        this.eventBus = eventBus;
        this.eventBus.register(this.eventLoop);
    }

    /* ----------- public methods ------------ */

    public void submit(List<TaskRun> taskRuns) {
        // 生成对应的TaskAttempt
        List<TaskAttempt> taskAttempts = taskRuns.stream()
                .map(this::createTaskAttempt).collect(Collectors.toList());

        save(taskAttempts);
        logger.debug("TaskAttempts saved. total={}", taskAttempts.size());

        for (TaskAttempt ta : taskAttempts) {
            eventLoop.post(ta.getId(), new StartWatchEvent(ta));
            logger.debug("Submitted TaskAttempt to EventLoop. taskAttemptId={}", ta.getId());
        }
    }

    /* ----------- private methods ------------ */

    private TaskAttempt createTaskAttempt(TaskRun taskRun) {
        checkNotNull(taskRun, "taskRun should not be null.");
        checkNotNull(taskRun.getId(), "taskRun's id should not be null.");

        int attempt = 1;
        TaskAttempt taskAttempt = TaskAttempt.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskAttemptId(taskRun.getId(), attempt))
                .withTaskRun(taskRun)
                .withAttempt(attempt)
                .withStatus(TaskRunStatus.CREATED)
                .build();
        logger.debug("Created taskAttempt. taskAttempt={}", taskAttempt);

        return taskAttempt;
    }

    private void save(List<TaskAttempt> taskAttempts) {
        for (TaskAttempt ta : taskAttempts) {
            taskRunDao.createAttempt(ta);
            taskRunDao.updateTaskAttemptStatus(ta.getId(), TaskRunStatus.CREATED);
        }
    }

    private class InnerEventLoop extends EventLoop<Long, Event> {
        public InnerEventLoop(int nThreads) {
            super("task-manager");
            addConsumers(IntStream.range(0, nThreads)
                    .mapToObj(i -> new Watcher())
                    .collect(Collectors.toList()));
        }

        @Subscribe
        public void onReceive(TaskAttemptStatusChangeEvent event) {
            post(event.getAttemptId(), event);
        }
    }

    private class Watcher extends EventConsumer<Long, Event> {
        private final WaitList<TaskAttempt, Long> waitList = new WaitList<>();

        @Override
        public void onReceive(Event event) {
            if (event instanceof StartWatchEvent) {
                startWatch(((StartWatchEvent) event).getTaskAttempt());
            }
        }

        private void startWatch(TaskAttempt taskAttempt) {
            // 首先注册对所有上游任务的监听（无论上游任务是否已经完成）
            List<Long> dependentTaskRunIds = taskAttempt.getTaskRun().getDependentTaskRunIds();
            Collection<Long> dependentTaskAttemptIds = queryAttemptIds(dependentTaskRunIds);
            logger.debug("Dependencies of taskAttempt {} is: DependentTaskRunIds={}, DependentTaskAttemptIds={}",
                    taskAttempt, dependentTaskRunIds, dependentTaskAttemptIds);

            for (Long dependentTaskAttemptId : dependentTaskAttemptIds) {
                listenTo(dependentTaskAttemptId, this::onUpstreamStatusChange);
            }
            waitList.addWait(taskAttempt, dependentTaskAttemptIds);

            // 向数据库check依赖的任务的状态
            List<TaskAttemptProps> attempts = taskRunDao.fetchLatestTaskAttempt(dependentTaskRunIds);
            for (TaskAttemptProps atp : attempts) {
                logger.debug("Fetched latest TaskAttempt. taskRunId={}, attempt={}, status={}",
                        atp.getTaskId(), atp.getAttempt(), atp.getStatus());
                if (atp.getStatus().isSuccess()) {
                    waitList.removeWait(atp.getId());
                    unlistenTo(atp.getId());
                }
            }

            executeIfPossible();
        }

        private void onUpstreamStatusChange(Event e) {
            if (e instanceof TaskAttemptStatusChangeEvent) {
                TaskAttemptStatusChangeEvent event = (TaskAttemptStatusChangeEvent) e;
                logger.debug("TaskAttempt status changed. TaskAttemptId={}, from={}, to={}",
                        event.getAttemptId(), event.getFromStatus(), event.getToStatus());

                if (event.getToStatus().isSuccess()) {
                    Long taskAttemptId = event.getAttemptId();
                    waitList.removeWait(taskAttemptId);
                    unlistenTo(taskAttemptId);
                }

                executeIfPossible();
            }
        }

        private Collection<Long> queryAttemptIds(List<Long> taskRunIds) {
            List<TaskAttemptProps> attemptProps = taskRunDao.fetchLatestTaskAttempt(taskRunIds);
            Map<Long, Long> attemptTaskRunMap = attemptProps.stream()
                    .collect(Collectors.toMap(i -> i.getTaskRunId(), i -> i.getId()));
            return attemptTaskRunMap.values();
        }

        private void executeIfPossible() {
            List<TaskAttempt> tasksCouldRun = waitList.pop();
            if (!tasksCouldRun.isEmpty()) {
                logger.debug("Submit taskAttempts to executor. taskAttempts={}", tasksCouldRun);
                for (TaskAttempt ta : tasksCouldRun) {
                    executor.submit(ta);
                }
            }
        }
    }

    private static class StartWatchEvent extends Event {
        private final TaskAttempt taskAttempt;

        public StartWatchEvent(TaskAttempt taskAttempt) {
            this.taskAttempt = taskAttempt;
        }

        public TaskAttempt getTaskAttempt() {
            return taskAttempt;
        }
    }
}
