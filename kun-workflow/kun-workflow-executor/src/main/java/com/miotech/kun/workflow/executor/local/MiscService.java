package com.miotech.kun.workflow.executor.local;

import com.google.common.eventbus.EventBus;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
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

@Singleton
public class MiscService {
    private static final Logger logger = LoggerFactory.getLogger(MiscService.class);

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private EventBus eventBus;

    @Inject
    private EventPublisher publisher;

    public void changeTaskAttemptStatus(long attemptId, TaskRunStatus status) {
        changeTaskAttemptStatus(attemptId, status, null, null);
    }

    public void changeTaskAttemptStatus(long attemptId, TaskRunStatus status,
                                        @Nullable OffsetDateTime startAt, @Nullable OffsetDateTime endAt) {
        logger.debug("Try to change TaskAttempt's status. attemptId={}, status={}, startAt={}, endAt={}", attemptId, status, startAt, endAt);
        TaskRunStatus prevStatus = taskRunDao.updateTaskAttemptStatus(attemptId, status, startAt, endAt)
                .orElseThrow(() -> new IllegalArgumentException(String.format("TaskAttempt with id %s not found.", attemptId)));
        eventBus.post(new TaskAttemptStatusChangeEvent(attemptId, prevStatus, status));

        Optional<TaskAttempt> attemptOptional = taskRunDao.fetchAttemptById(attemptId);
        if(attemptOptional.isPresent()){
            TaskAttempt attempt = attemptOptional.get();
            publisher.publish(new TaskAttemptStatusChangeEvent(attemptId, prevStatus, status, attempt.getTaskName(), attempt.getTaskId()));
        }
    }
}
