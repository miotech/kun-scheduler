package com.miotech.kun.workflow.executor.local;

import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueManage {
    private Map<String, TaskAttemptQueue> queueMap;
    private Lock lock = new ReentrantLock();
    private Condition hasElement = lock.newCondition();
    private static Logger logger = LoggerFactory.getLogger(QueueManage.class);
    private final Integer DEFAULT_WORKER_TOKEN_SIZE = 8;
    private final String DEFAULT_QUEUE = "default";


    public QueueManage(Props props) {
        queueMap = new ConcurrentHashMap<>();
        List<String> queueNames = props.getStringList("executor.queue");
        logger.info("init queueManage , size = {}", queueNames.size());
        if (queueNames.size() == 0) {
            TaskAttemptQueue queue = new TaskAttemptQueue(DEFAULT_QUEUE, DEFAULT_WORKER_TOKEN_SIZE);
            queueMap.put(queue.getName(), queue);
        }
        for (String queueName : queueNames) {
            String prefix = "executor.queue." + queueName;
            Integer capacity = props.getInt(prefix + ".capacity", 0);
            TaskAttemptQueue queue = new TaskAttemptQueue(queueName, capacity);
            logger.info("init queue name = {}, capacity = {}", queueName, capacity);
            queueMap.put(queueName, queue);
        }
    }

    //提交TaskAttempt
    public void submit(TaskAttempt taskAttempt) {
        lock.lock();
        try {
            Preconditions.checkNotNull(taskAttempt.getQueueName(), "Invalid argument `queueName`: null");
            TaskAttemptQueue queue = queueMap.get(taskAttempt.getQueueName());
            if (queue == null) {
                throw new NoSuchElementException("no such queue,name = " + taskAttempt.getQueueName());
            }
            queue.add(taskAttempt);
            if (queue.getSize() == 1) {
                logger.debug("queue = {} is not empty,wake up consumer", queue.getName());
                hasElement.signal();
            }
        } finally {
            lock.unlock();
        }

    }

    public TaskAttemptQueue getTaskAttemptQueue(String queueName) {
        return queueMap.get(queueName);
    }

    //删除TaskAttempt
    public boolean remove(TaskAttempt taskAttempt) {
        lock.lock();
        logger.info("going to remove taskAttempt from queue , attemptId = {},queueName = {}", taskAttempt.getId(), taskAttempt.getQueueName());
        try {
            String queueName = taskAttempt.getQueueName();
            TaskAttemptQueue queue = queueMap.get(queueName);
            if (queue == null) {
                throw new NoSuchElementException("no such queue,name = " + queueName);
            }
            boolean result = queue.remove(taskAttempt);
            return result;
        } finally {
            lock.unlock();
        }
    }

    public TaskAttempt take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (!hasElementToTake()) {
                hasElement.await();
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        for (Map.Entry<String, TaskAttemptQueue> entry : queueMap.entrySet()) {
            TaskAttemptQueue queue = entry.getValue();
            if (!queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasElementToTake() {
        for (Map.Entry<String, TaskAttemptQueue> entry : queueMap.entrySet()) {
            TaskAttemptQueue queue = entry.getValue();
            if (!queue.isEmpty() && queue.hasCapacity()) {
                return true;
            }
        }
        return false;
    }

    public void release(String queueName) {
        lock.lock();
        try {
            TaskAttemptQueue queue = queueMap.get(queueName);
            if (queue == null) {
                throw new NoSuchElementException("no such queue,name = " + queueName);
            }
            queue.release();
            if (queue.getRemainCapacity() == 1) {
                logger.debug("queue = {} release resource,wake up consumer", queue.getName());
                hasElement.signal();
            }
        } finally {
            lock.unlock();
        }

    }

    public void reset() {
        for (Map.Entry<String, TaskAttemptQueue> entry : queueMap.entrySet()) {
            TaskAttemptQueue queue = entry.getValue();
            queue.reset();
        }
    }

    public void recover(String queueName) {
        lock.lock();
        try {
            TaskAttemptQueue queue = queueMap.get(queueName);
            if (queue == null) {
                throw new NoSuchElementException("no such queue,name = " + queueName);
            }
            logger.debug("recover taskAttempt for queue : {}", queueName);
            queue.acquire();
        } finally {
            lock.unlock();
        }

    }

    private TaskAttempt dequeue() {
        for (Map.Entry<String, TaskAttemptQueue> entry : queueMap.entrySet()) {
            TaskAttemptQueue queue = entry.getValue();
            if (!queue.isEmpty() && queue.hasCapacity()) {
                return queue.take();
            }
        }
        throw new IllegalStateException("cannot take any taskAttempt from queue");
    }


}
