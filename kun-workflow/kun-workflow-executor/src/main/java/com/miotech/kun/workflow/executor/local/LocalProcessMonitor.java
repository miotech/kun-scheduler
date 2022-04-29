package com.miotech.kun.workflow.executor.local;

import com.miotech.kun.workflow.executor.WorkerEventHandler;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LocalProcessMonitor implements WorkerMonitor {

    private final Logger logger = LoggerFactory.getLogger(LocalProcessMonitor.class);
    private Map<Long, WorkerEventHandler> registerHandlers = new ConcurrentHashMap<>();
    private final long POLLING_PERIOD = 5 * 1000;

    private final LocalProcessBackend localProcessBackend;

    private ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);

    public LocalProcessMonitor(LocalProcessBackend localProcessBackend) {
        this.localProcessBackend = localProcessBackend;
    }

    @Override
    public void start() {
        logger.info("start process monitor...");
        timer.scheduleAtFixedRate(new PollingProcessStatus(), 10, POLLING_PERIOD, TimeUnit.MILLISECONDS);
        localProcessBackend.watch(new LocalProcessWatcher());
    }

    public void stop(){
        timer.shutdown();
    }

    @Override
    public boolean register(Long taskAttemptId, WorkerEventHandler handler) {
        logger.debug("register process event handler,taskAttemptId = {}", taskAttemptId);
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


    //监控process状态变更
    class LocalProcessWatcher implements ProcessWatcher  {

        public void eventReceived(ProcessSnapShot processSnapShot) {
            Long taskAttemptId = processSnapShot.getIns().getTaskAttemptId();
            logger.debug("receive process event taskAttemptId = {}", taskAttemptId);
            WorkerEventHandler workerEventHandler = registerHandlers.get(taskAttemptId);
            if (workerEventHandler == null) {
                logger.warn("process with taskAttemptId = {} count not found event handler", taskAttemptId);
                return;
            }
            workerEventHandler.onReceiveSnapshot(processSnapShot);
        }

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
