package com.miotech.kun.workflow.executor.local;

import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.executor.ExecutorBackEnd;
import com.miotech.kun.workflow.executor.WorkerEventHandler;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Singleton
public class LocalProcessMonitor implements WorkerMonitor, ExecutorBackEnd, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(LocalProcessMonitor.class);
    private Map<Long, WorkerEventHandler> registerHandlers = new ConcurrentHashMap<>();
    private final long POLLING_PERIOD = 5 * 1000;

    private final LocalProcessBackend localProcessBackend;

    @Inject
    public LocalProcessMonitor(LocalProcessBackend localProcessBackend) {
        this.localProcessBackend = localProcessBackend;
    }

    public void start() {
        logger.info("start pod monitor...");
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);
        timer.scheduleAtFixedRate(new PollingProcessStatus(), 10, POLLING_PERIOD, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean register(Long taskAttemptId, WorkerEventHandler handler) {
        logger.debug("register pod event handler,taskAttemptId = {}", taskAttemptId);
        registerHandlers.put(taskAttemptId, handler);
        return true;
    }

    @Override
    public boolean unRegister(Long taskAttemptId) {
        logger.debug("unRegister worker,taskAttemptId = {}", taskAttemptId);
        registerHandlers.remove(taskAttemptId);
        return true;
    }

    @Override
    public void unRegisterAll() {
        registerHandlers.clear();
    }

    @Override
    public boolean statusUpdate(TaskAttemptMsg msg) {
        logger.info("taskAttempt status change attemptMsg = {}", msg);
        ProcessSnapShot processSnapShot = ProcessSnapShot.fromTaskAttemptMessage(msg);
        WorkerEventHandler workerEventHandler = registerHandlers.get(msg.getTaskAttemptId());
        if (workerEventHandler == null) {
            logger.warn("process with taskAttemptId = {} count not found event handler", msg.getTaskAttemptId());
            return true;
        }
        workerEventHandler.onReceiveSnapshot(processSnapShot);
        return true;
    }

    @Override
    public boolean heartBeatReceive(HeartBeatMessage heartBeatMessage) {
        logger.debug("get heart beat from worker = {}", heartBeatMessage);
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        start();
    }

    class PollingProcessStatus implements Runnable {
        @Override
        public void run() {
            List<ProcessSnapShot> runningProcess = localProcessBackend.fetchRunningProcess();
            Set<Long> registerSet = new HashSet<>(registerHandlers.keySet());
            for (ProcessSnapShot processSnapShot : runningProcess) {
                Long taskAttemptId = processSnapShot.getIns().getTaskAttemptId();
                WorkerEventHandler workerEventHandler = registerHandlers.get(taskAttemptId);
                if (workerEventHandler == null) {
                    logger.warn("process with taskAttemptId = {} count not found event handler", taskAttemptId);
                    continue;
                }
                workerEventHandler.onReceivePollingSnapShot(processSnapShot);
                registerSet.remove(taskAttemptId);
            }
            for (Long unFoundAttempt : registerSet) {
                logger.warn("count not found process for register handler, taskAttemptId = {}", unFoundAttempt);
            }
        }
    }
}
