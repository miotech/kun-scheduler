package com.miotech.kun.workflow.executor.kubernetes.mock;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.WorkerEventHandler;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockWorkerMonitor implements WorkerMonitor {

    private final Logger logger = LoggerFactory.getLogger(MockWorkerMonitor.class);

    private Map<Long, WorkerEventHandler> registerHandlers = new ConcurrentHashMap<>();

    @Override
    public boolean register(Long taskAttemptId, WorkerEventHandler handler) {
        logger.debug("register worker event handler,taskAttemptId = {}", taskAttemptId);
        registerHandlers.put(taskAttemptId, handler);
        return true;
    }

    @Override
    public boolean unRegister(Long taskAttemptId) {
        logger.debug("unRegister worker event handler,taskAttemptId = {}", taskAttemptId);
        registerHandlers.remove(taskAttemptId);
        return true;
    }

    @Override
    public void unRegisterAll() {

    }

    public void makeDone(Long taskAttemptId) {
        WorkerInstance instance = WorkerInstance.newBuilder()
                .withTaskAttemptId(taskAttemptId).build();
        WorkerSnapshot workerSnapshot = new WorkerSnapshot(instance, DateTimeUtils.now()) {
            @Override
            public TaskRunStatus getStatus() {
                return TaskRunStatus.SUCCESS;
            }
        };
        registerHandlers.get(taskAttemptId).onReceiveSnapshot(workerSnapshot);
    }

    public void makeFailed(Long taskAttemptId) {
        WorkerInstance instance = WorkerInstance.newBuilder()
                .withTaskAttemptId(taskAttemptId).build();
        WorkerSnapshot workerSnapshot = new WorkerSnapshot(instance, DateTimeUtils.now()) {
            @Override
            public TaskRunStatus getStatus() {
                return TaskRunStatus.FAILED;
            }
        };
        registerHandlers.get(taskAttemptId).onReceiveSnapshot(workerSnapshot);
    }

    public void makeRunning(Long taskAttemptId) {
        WorkerInstance instance = WorkerInstance.newBuilder()
                .withTaskAttemptId(taskAttemptId).build();
        WorkerSnapshot workerSnapshot = new WorkerSnapshot(instance, DateTimeUtils.now()) {
            @Override
            public TaskRunStatus getStatus() {
                return TaskRunStatus.RUNNING;
            }
        };
        registerHandlers.get(taskAttemptId).onReceiveSnapshot(workerSnapshot);
    }

    public List<WorkerInstance> allRegister(){
        List<WorkerInstance> registerWorkers = new ArrayList<>();
        for(Long taskAttemptId : registerHandlers.keySet()){
            WorkerInstance instance = WorkerInstance.newBuilder()
                    .withTaskAttemptId(taskAttemptId).build();
            registerWorkers.add(instance);
        }
        return registerWorkers;
    }

    public boolean hasRegister(Long taskAttemptId){
        return registerHandlers.containsKey(taskAttemptId);
    }
}
