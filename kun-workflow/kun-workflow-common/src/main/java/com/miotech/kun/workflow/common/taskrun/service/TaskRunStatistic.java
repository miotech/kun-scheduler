package com.miotech.kun.workflow.common.taskrun.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.math.Stats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class TaskRunStatistic implements InitializingBean {

    private final TaskRunDao taskRunDao;

    private final EventBus eventBus;

    private final InnerEventLoop eventLoop;

    @Inject
    public TaskRunStatistic(TaskRunDao taskRunDao, EventBus eventBus) {
        this.taskRunDao = taskRunDao;
        this.eventBus = eventBus;
        this.eventLoop = new InnerEventLoop();
    }

    public void start() {
        eventBus.register(this.eventLoop);
        eventLoop.start();
    }


    private void updateTaskRunStat(Long taskRunId) {
        TaskRun taskRun = taskRunDao.fetchTaskRunById(taskRunId).get();
        List<TaskRun> previousTaskRuns = taskRunDao.fetchLatestTaskRuns(taskRun.getTask().getId(), ImmutableList.of(TaskRunStatus.SUCCESS), 7);
        Long averageRunningTime;
        Long averageQueuingTime;
        if (previousTaskRuns.isEmpty()) {
            averageRunningTime = 0L;
            averageQueuingTime = 0L;
        } else {
            List<Long> runningTime = previousTaskRuns.stream()
                    .map(x -> x.getStartAt().until(x.getEndAt(), ChronoUnit.SECONDS))
                    .collect(Collectors.toList());
            List<Long> queuingTime = previousTaskRuns.stream()
                    .map(x -> x.getQueuedAt().until(x.getStartAt(), ChronoUnit.SECONDS))
                    .collect(Collectors.toList());
            averageRunningTime = Math.round(Stats.meanOf(runningTime));
            averageQueuingTime = Math.round(Stats.meanOf(queuingTime));
        }
        taskRunDao.updateTaskRunStat(taskRunId, averageRunningTime, averageQueuingTime);
    }

    @Override
    public void afterPropertiesSet() {
        start();
    }

    private class InnerEventLoop extends EventLoop<Long, Event> {
        public InnerEventLoop() {
            super("taskrun-statistic");
            addConsumers(Lists.newArrayList(
                    new EventConsumer<Long, Event>() {
                        @Override
                        public void onReceive(Event event) {
                            if (event instanceof TaskRunCreatedEvent) {
                                TaskRunCreatedEvent taskRunCreatedEvent = (TaskRunCreatedEvent) event;
                                updateTaskRunStat(taskRunCreatedEvent.getTaskRunId());
                            }
                        }
                    }));
        }

        @Subscribe
        public void onReceive(TaskRunCreatedEvent event) {
            post(event.getTaskRunId(), event);
        }
    }
}
