package com.miotech.kun.workflow.executor.local;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class TaskAttemptQueue {
    private Queue<TaskAttempt> queue;
    private final Integer capacity;//队列资源容量
    private Integer remainCapacity;
    private String name;
    private static Logger logger = LoggerFactory.getLogger(TaskAttemptQueue.class);

    public Integer getCapacity() {
        return capacity;
    }

    public String getName() {
        return name;
    }

    public Integer getSize(){
        return queue.size();
    }

    public TaskAttemptQueue(String name, Integer capacity) {
        queue = new LinkedList<>();
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
        logger.debug("taskAttemptId = {} acquire worker token from queue : {}, current size = {}, max size = {}", taskAttempt.getId(), name, remainCapacity, capacity);
        return taskAttempt;

    }

    public synchronized void add(TaskAttempt taskAttempt) {
        queue.add(taskAttempt);
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
                return true;
            }
        }
        logger.warn("could not found taskAttempt from queue , attemptId = {},queueName = {}", taskAttempt.getId(), taskAttempt.getQueueName());
        return false;
    }

    public synchronized void release() {
        if (remainCapacity < capacity) {
            remainCapacity++;
            logger.debug("release worker token from queue : {}, current size = {},max size = {}", name, remainCapacity, capacity);

        } else {
            throw new IllegalStateException("remain resource cannot exceed capacity");
        }
    }

    public synchronized void acquire() {
        if (remainCapacity <= 0) {
            throw new IllegalStateException("there are no resources left in queue : " + name);
        }
        remainCapacity--;
        logger.debug("acquire worker token from queue : {}, current size = {},max size = {}", name, remainCapacity, capacity);
    }

    public void reset() {
        queue.clear();
        remainCapacity = capacity;
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
