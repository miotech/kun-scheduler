package com.miotech.kun.workflow.executor;

import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.executor.local.MiscService;
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

    private Map<String, TaskAttemptQueue> queueMap;
    private Lock lock = new ReentrantLock();
    protected Props props;
    private MiscService miscService;

    public AbstractQueueManager(Props props, MiscService miscService) {
        this.props = props;
        queueMap = new ConcurrentHashMap<>();
        this.miscService = miscService;
    }

    public void init() {//根据配置文件初始化nameSpace资源配额
        logger.debug("load resource configuration...");
        List<ResourceQueue> resourceQueueList = loadResourceQueue();
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

    public List<ResourceQueue> loadResourceQueue() {
        List<String> queueNames = props.getStringList("executor.env.resourceQueues");
        List<ResourceQueue> queueList = new ArrayList<>();
        for (String queueName : queueNames) {
            String prefix = "executor.env.resourceQueues." + queueName;
            Integer cores = props.getInt(prefix + ".quota.cores", 0);
            Integer memory = props.getInt(prefix + ".quota.memory", 0);
            Integer workerNumbers = props.getInt(prefix + ".quota.workerNumbers", 0);
            ResourceQueue resourceQueue = ResourceQueue.newBuilder()
                    .withQueueName(queueName)
                    .withCores(cores)
                    .withMemory(memory)
                    .withWorkerNumbers(workerNumbers)
                    .build();
            logger.info("init queue = {}", resourceQueue);
            queueList.add(resourceQueue);
        }
        return queueList;
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
            logger.debug("submit taskAttempt = {} to queue = {}", taskAttempt.getId(), queue.getName());
            queue.add(taskAttempt);
            miscService.changeTaskAttemptStatus(taskAttempt.getId(), TaskRunStatus.QUEUED);
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

    public TaskAttempt take() {
        for (Map.Entry<String, TaskAttemptQueue> entry : queueMap.entrySet()) {
            TaskAttemptQueue queue = entry.getValue();
            if (!queue.isEmpty() && hasCapacity(queue)) {
                return queue.take();
            }
        }
        return null;
    }

    public void changePriority(long taskAttemptId, String queueName, TaskPriority priority) {
        lock.lock();
        try {
            TaskAttemptQueue queue = queueMap.get(queueName);
            if (queue == null) {
                throw new NoSuchElementException("no such queue,name = " + queueName);
            }
            queue.changePriority(taskAttemptId, priority.getPriority());
        } finally {
            lock.unlock();
        }
    }

    public abstract boolean hasCapacity(TaskAttemptQueue taskAttemptQueue);

    public abstract ResourceQueue createResourceQueue(ResourceQueue resourceQueue);

    public abstract ResourceQueue updateResourceQueue(ResourceQueue resourceQueue);

}

