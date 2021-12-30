package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

public class EventMapperTest {

    @Test
    public void testLineageEvent() throws JsonProcessingException {
        Long taskId = IdGenerator.getInstance().nextId();
        LineageEvent lineageEvent = new LineageEvent(taskId, ImmutableList.of(), ImmutableList.of());

        String json = EventMapper.toJson(lineageEvent);
        Event event = EventMapper.toEvent(json);
        assertThat(event, isA(LineageEvent.class));
        LineageEvent createdEvent = (LineageEvent) event;
        assertThat(createdEvent, sameBeanAs(lineageEvent));
    }

    @Test
    public void testTickEvent() throws JsonProcessingException {
        TickEvent tickEvent = new TickEvent(new Tick(OffsetDateTime.now()));

        String json = EventMapper.toJson(tickEvent);
        Event event = EventMapper.toEvent(json);
        assertThat(event, isA(TickEvent.class));
        TickEvent createdEvent = (TickEvent) event;
        assertThat(createdEvent, sameBeanAs(tickEvent));
    }

    @Test
    public void testTaskAttemptStatusChangeEvent() throws JsonProcessingException {
        TaskAttemptStatusChangeEvent taskAttemptStatusChangeEvent = new TaskAttemptStatusChangeEvent(IdGenerator.getInstance().nextId(),
                TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", IdGenerator.getInstance().nextId());

        String json = EventMapper.toJson(taskAttemptStatusChangeEvent);
        Event event = EventMapper.toEvent(json);
        assertThat(event, isA(TaskAttemptStatusChangeEvent.class));
        TaskAttemptStatusChangeEvent createdEvent = (TaskAttemptStatusChangeEvent) event;
        assertThat(createdEvent, sameBeanAs(taskAttemptStatusChangeEvent));
    }

    @Test
    public void testTaskAttemptFinishedEvent() throws JsonProcessingException {
        TaskAttemptFinishedEvent taskAttemptFinishedEvent = new TaskAttemptFinishedEvent(IdGenerator.getInstance().nextId(),
                IdGenerator.getInstance().nextId(),IdGenerator.getInstance().nextId(),TaskRunStatus.CREATED, ImmutableList.of(), ImmutableList.of());

        String json = EventMapper.toJson(taskAttemptFinishedEvent);
        Event event = EventMapper.toEvent(json);
        assertThat(event, isA(TaskAttemptFinishedEvent.class));
        TaskAttemptFinishedEvent createdEvent = (TaskAttemptFinishedEvent) event;
        assertThat(createdEvent, sameBeanAs(taskAttemptFinishedEvent));
    }

    @Test
    public void testTaskRunCreatedEvent() throws JsonProcessingException {
        Long taskId = IdGenerator.getInstance().nextId();
        Long taskRunId = IdGenerator.getInstance().nextId();
        TaskRunCreatedEvent taskRunCreatedEvent = new TaskRunCreatedEvent(taskId, taskRunId);

        String json = EventMapper.toJson(taskRunCreatedEvent);
        Event event = EventMapper.toEvent(json);
        assertThat(event, isA(TaskRunCreatedEvent.class));
        TaskRunCreatedEvent createdEvent = (TaskRunCreatedEvent) event;
        assertThat(createdEvent, sameBeanAs(taskRunCreatedEvent));
    }

    @Test
    public void testCheckResultEvent() throws JsonProcessingException {
        Long taskRunId = IdGenerator.getInstance().nextId();
        CheckResultEvent checkResultEvent = new CheckResultEvent(taskRunId,true);

        String json = EventMapper.toJson(checkResultEvent);
        Event event = EventMapper.toEvent(json);
        assertThat(event, isA(CheckResultEvent.class));
        CheckResultEvent createdEvent = (CheckResultEvent) event;
        assertThat(createdEvent, sameBeanAs(checkResultEvent));
    }

}
