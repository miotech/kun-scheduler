package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttemptInQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskPriorityComparator;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TaskAttemptQueue {
    private Queue<TaskAttemptInQueue> queue;
    private final ResourceQueue resourceQueue;//队列资源容量
    private String name;
    private static Logger logger = LoggerFactory.getLogger(TaskAttemptQueue.class);

    private Set<Long> dispatchedTaskAttempt = new HashSet<>();


    public ResourceQueue getResourceQueue() {
        return resourceQueue;
    }

    public String getName() {
        return name;
    }

    public Integer getSize() {
        return queue.size();
    }

    public void reset(){
        queue.clear();
    }

    public TaskAttemptQueue(ResourceQueue resourceQueue) {
        queue = new PriorityQueue<>(new TaskPriorityComparator());
        this.name = resourceQueue.getQueueName();
        this.resourceQueue = resourceQueue;
    }

    public synchronized TaskAttempt take() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        TaskAttempt taskAttempt = queue.poll().getTaskAttempt();
        dispatchedTaskAttempt.add(taskAttempt.getId());
        logger.debug("queue = {} has {} taskAttempt in queue", getName(), getSize());
        return taskAttempt;

    }

    public synchronized void add(TaskAttempt taskAttempt) throws IllegalStateException {
        if (containsAttempt(taskAttempt)) {
            throw new IllegalStateException("taskAttemptId = " + taskAttempt.getId() + "has been submit to queue = " + taskAttempt.getQueueName());
        }
        queue.add(new TaskAttemptInQueue(taskAttempt, DateTimeUtils.now()));
        logger.debug("queue = {} has {} taskAttempt in queue", getName(), getSize());
    }

    public synchronized boolean remove(TaskAttempt taskAttempt) {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Iterator<TaskAttemptInQueue> iterator = queue.iterator();
        while (iterator.hasNext()) {
            TaskAttempt queued = iterator.next().getTaskAttempt();
            if (queued.getId().equals(taskAttempt.getId())) {
                iterator.remove();
                logger.info("remove taskAttempt from queue , attemptId = {},queueName = {}", taskAttempt.getId(), taskAttempt.getQueueName());
                logger.debug("queue = {} has {} taskAttempt in queue", getName(), getSize());
                return true;
            }
        }
        logger.warn("could not found taskAttempt from queue , attemptId = {},queueName = {}", taskAttempt.getId(), taskAttempt.getQueueName());
        return false;
    }


    public synchronized void changePriority(long attemptId, int priority) {
        TaskAttempt queued = getTaskAttemptById(attemptId);
        if (queued == null) {
            throw new IllegalStateException("taskAttempt = " + attemptId +
                    " to change priority is not in queue");
        }
        TaskAttempt change = queued.cloneBuilder().withPriority(priority).build();
        remove(queued);
        add(change);

    }

    public boolean containsAttempt(TaskAttempt taskAttempt) {
        return getTaskAttemptById(taskAttempt.getId()) != null;
    }

    public TaskAttempt getTaskAttemptById(Long attemptId) {
        Iterator<TaskAttemptInQueue> iterator = queue.iterator();
        while (iterator.hasNext()) {
            TaskAttempt queued = iterator.next().getTaskAttempt();
            if (queued.getId().equals(attemptId)) {
                return queued;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return queue.size() == 0;
    }

}

