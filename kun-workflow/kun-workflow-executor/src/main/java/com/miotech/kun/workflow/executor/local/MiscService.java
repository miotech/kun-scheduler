package com.miotech.kun.workflow.executor.local;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.PublicEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.utils.DateTimeUtils;
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
        // termAt is equal to endAt or current time
        OffsetDateTime termAt = null;
        if (status.isTermState()) {
            termAt = endAt != null ? endAt : DateTimeUtils.now();
        }

        logger.info("Try to change TaskAttempt's status. attemptId={}, status={}, startAt={}, endAt={}, termAt={}", attemptId, status, startAt, endAt, termAt);
        TaskRunStatus prevStatus = taskRunDao.updateTaskAttemptStatus(attemptId, status, startAt, endAt, termAt)
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
        PublicEventListener listener = new PublicEventListener();
        eventBus.register(listener);
    }

    private class PublicEventListener {
        @Subscribe
        public void publicEvent(PublicEvent event) {
            publisher.publish(event);
        }
    }

    private TaskAttempt loadTaskAttempt(Long attemptId){
        Optional<TaskAttempt> attemptOptional = taskRunDao.fetchAttemptById(attemptId);
        return attemptOptional.get();
    }
}
