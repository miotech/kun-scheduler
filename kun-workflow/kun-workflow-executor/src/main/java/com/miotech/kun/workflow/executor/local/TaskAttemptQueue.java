package com.miotech.kun.workflow.executor.local;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskPriorityComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * this queue is not thread safe
 */
public class TaskAttemptQueue {
    private Queue<TaskAttempt> queue;
    private final Integer capacity;//队列资源容量
    private Integer remainCapacity;
    private String name;
    private static Logger logger = LoggerFactory.getLogger(TaskAttemptQueue.class);

    private Set<Long> dispatchedTaskAttempt = new HashSet<>();

    public Integer getCapacity() {
        return capacity;
    }

    public String getName() {
        return name;
    }

    public Integer getSize() {
        return queue.size();
    }

    public TaskAttemptQueue(String name, Integer capacity) {
        queue = new PriorityQueue<>(new TaskPriorityComparator());
        this.name = name;
        this.capacity = capacity;
        remainCapacity = capacity;
    }

    public synchronized TaskAttempt take() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        if (remainCapacity <= 0) {
            throw new IllegalStateException("there are no resources left in queue : " + name);
        }
        remainCapacity--;
        TaskAttempt taskAttempt = queue.poll();
        dispatchedTaskAttempt.add(taskAttempt.getId());
        logger.debug("taskAttemptId = {} acquire worker token from queue : {}, current size = {}, max size = {}", taskAttempt.getId(), name, remainCapacity, capacity);
        logger.debug("queue = {} has {} taskAttempt in queue", getName(), getSize());
        return taskAttempt;

    }

    public synchronized void add(TaskAttempt taskAttempt) {
        queue.add(taskAttempt);
        logger.debug("queue = {} has {} taskAttempt in queue", getName(), getSize());
    }

    public synchronized boolean remove(TaskAttempt taskAttempt) {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Iterator<TaskAttempt> iterator = queue.iterator();
        while (iterator.hasNext()) {
            TaskAttempt queued = iterator.next();
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

    public synchronized void release(Long taskAttemptId) {
        if (remainCapacity < capacity) {
            if (!dispatchedTaskAttempt.contains(taskAttemptId)) {
                throw new IllegalStateException("token with taskAttemptId = " + taskAttemptId + "has been released");
            }
            dispatchedTaskAttempt.remove(taskAttemptId);
            remainCapacity++;
            logger.debug("release worker token from queue : {}, current size = {},max size = {}", name, remainCapacity, capacity);

        } else {
            throw new IllegalStateException("remain resource cannot exceed capacity");
        }
    }

    public synchronized void acquire(Long taskAttemptId) {
        if (remainCapacity <= 0) {
            throw new IllegalStateException("there are no resources left in queue : " + name);
        }
        if (!dispatchedTaskAttempt.add(taskAttemptId)) {
            throw new IllegalStateException("taskAttemptId = " + taskAttemptId + "already get the token");
        }
        remainCapacity--;
        logger.debug("acquire worker token from queue : {}, current size = {},max size = {}", name, remainCapacity, capacity);
    }

    public synchronized void reset() {
        queue.clear();
        remainCapacity = capacity;
        dispatchedTaskAttempt.clear();
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
        Iterator<TaskAttempt> iterator = queue.iterator();
        while (iterator.hasNext()) {
            TaskAttempt queued = iterator.next();
            if (queued.getId().equals(attemptId)) {
                return queued;
            }
        }
        return null;
    }

    public boolean hasCapacity() {
        return remainCapacity > 0;
    }

    public Integer getRemainCapacity() {
        return remainCapacity;
    }

    public boolean isEmpty() {
        return queue.size() == 0;
    }

}
