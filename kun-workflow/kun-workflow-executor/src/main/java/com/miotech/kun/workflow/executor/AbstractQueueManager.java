package com.miotech.kun.workflow.executor;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public abstract class AbstractQueueManager {

    private Logger logger = LoggerFactory.getLogger(AbstractQueueManager.class);

    protected Map<String, TaskAttemptQueue> queueMap;
    private Lock lock = new ReentrantLock();
    @Inject
    private EventBus eventBus;
    private final String name;
    private final List<ResourceQueue> resourceQueueList;
    protected final ExecutorConfig executorConfig;

    public AbstractQueueManager(ExecutorConfig executorConfig, String name) {
        this.executorConfig = executorConfig;
        this.resourceQueueList = executorConfig.getResourceQueues();
        queueMap = new ConcurrentHashMap<>();
        this.name = name;
    }

    public void init() {//根据配置文件初始化nameSpace资源配额
        logger.debug("init taskAttempt Queue...");
        List<TaskAttemptQueue> taskAttemptQueueList = initTaskAttemptQueue(resourceQueueList);
        for (TaskAttemptQueue taskAttemptQueue : taskAttemptQueueList) {
            queueMap.put(taskAttemptQueue.getName(), taskAttemptQueue);
        }
    }

    public List<TaskAttemptQueue> initTaskAttemptQueue(List<ResourceQueue> resourceQueueList) {
        List<TaskAttemptQueue> taskAttemptQueueList = new ArrayList<>();
        for (ResourceQueue resourceQueue : resourceQueueList) {
            TaskAttemptQueue taskAttemptQueue = new TaskAttemptQueue(resourceQueue);
            taskAttemptQueueList.add(taskAttemptQueue);
        }
        return taskAttemptQueueList;
    }

    public void injectMember(Injector injector){
        injector.injectMembers(this);
    }


    //提交TaskAttempt
    public void submit(TaskAttempt taskAttempt) {
        lock.lock();
        try {
            logger.info("{} submit task attempt {}", name, taskAttempt.getId());
            Preconditions.checkNotNull(taskAttempt.getQueueName(), "Invalid argument `queueName`: null");
            TaskAttemptQueue queue = queueMap.get(taskAttempt.getQueueName());
            if (queue == null) {
                throw new NoSuchElementException("no such queue,name = " + taskAttempt.getQueueName());
            }
            logger.debug("submit taskAttempt = {} to queue = {}", taskAttempt.getId(), queue.getName());
            queue.add(taskAttempt);
        } finally {
            lock.unlock();
        }

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

    public List<TaskAttempt> drain() {
        List<TaskAttempt> readyTaskAttemptList = new ArrayList<>();
        for (Map.Entry<String, TaskAttemptQueue> entry : queueMap.entrySet()) {
            TaskAttemptQueue queue = entry.getValue();
            Integer capacity = getCapacity(queue);
            while (capacity > 0 && !queue.isEmpty()) {
                readyTaskAttemptList.add(queue.take());
                capacity--;
            }
        }
        return readyTaskAttemptList;
    }

    public void changePriority(long taskAttemptId, String queueName, Integer priority) {
        lock.lock();
        try {
            TaskAttemptQueue queue = queueMap.get(queueName);
            if (queue == null) {
                throw new NoSuchElementException("no such queue,name = " + queueName);
            }
            queue.changePriority(taskAttemptId, priority);
        } finally {
            lock.unlock();
        }
    }

    public Integer getQueuedNum(String queueName) {
        return queueMap.get(queueName).getSize();
    }

    public ResourceQueue getResourceQueue(String queueName){
        return queueMap.get(queueName).getResourceQueue();
    }

    public Integer getCapacity(String queueName){
        TaskAttemptQueue taskAttemptQueue = queueMap.get(queueName);
        return getCapacity(taskAttemptQueue);
    }

    //just for testing
    public void reset(){
        for (Map.Entry<String, TaskAttemptQueue> entry : queueMap.entrySet()) {
            TaskAttemptQueue queue = entry.getValue();
            queue.reset();
        }
    }

    public abstract Integer getCapacity(TaskAttemptQueue taskAttemptQueue);

    public abstract ResourceQueue createResourceQueue(ResourceQueue resourceQueue);

    public abstract ResourceQueue updateResourceQueue(ResourceQueue resourceQueue);

}

