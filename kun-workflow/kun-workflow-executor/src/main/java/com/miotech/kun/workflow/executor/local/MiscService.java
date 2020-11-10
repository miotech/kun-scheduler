package com.miotech.kun.workflow.executor.local;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Singleton
public class MiscService {
    private static final Logger logger = LoggerFactory.getLogger(MiscService.class);

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private EventBus eventBus;

    @Inject
    private EventPublisher publisher;

    private final LoadingCache<Long, TaskAttempt> taskAttemptCache = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .build(new CacheLoader<Long, TaskAttempt>() {
                @Override
                public TaskAttempt load(Long attemptId) throws Exception {
                    return loadTaskAttempt(attemptId);
                }
            });

    public void changeTaskAttemptStatus(long attemptId, TaskRunStatus status) {
        changeTaskAttemptStatus(attemptId, status, null, null);
    }

    public void changeTaskAttemptStatus(long attemptId, TaskRunStatus status,
                                        @Nullable OffsetDateTime startAt, @Nullable OffsetDateTime endAt) {
        logger.debug("Try to change TaskAttempt's status. attemptId={}, status={}, startAt={}, endAt={}", attemptId, status, startAt, endAt);
        TaskRunStatus prevStatus = taskRunDao.updateTaskAttemptStatus(attemptId, status, startAt, endAt)
                .orElseThrow(() -> new IllegalArgumentException(String.format("TaskAttempt with id %s not found.", attemptId)));

        TaskAttempt attempt = null;
        try {
            attempt = taskAttemptCache.get(attemptId);
            eventBus.post(new TaskAttemptStatusChangeEvent(attemptId, prevStatus, status, attempt.getTaskName(), attempt.getTaskId()));
        } catch (ExecutionException e) {
            logger.error(String.format("failed to load taskAttempt from cahce, attempId %d", attemptId), e);
        }catch (Exception e){
            logger.error(String.format("task not found from attempId %d", attemptId), e);
        }

    }

    @Inject
    public void init() {
        TaskAttemptStatusChangeEventListener listener = new TaskAttemptStatusChangeEventListener();
        eventBus.register(listener);
    }

    private class TaskAttemptStatusChangeEventListener {
        @Subscribe
        public void taskAttemptStatusChangeEvent(TaskAttemptStatusChangeEvent event) {
            publisher.publish(event);
        }
    }

    private TaskAttempt loadTaskAttempt(Long attemptId){
        Optional<TaskAttempt> attemptOptional = taskRunDao.fetchAttemptById(attemptId);
        return attemptOptional.get();
    }
}
