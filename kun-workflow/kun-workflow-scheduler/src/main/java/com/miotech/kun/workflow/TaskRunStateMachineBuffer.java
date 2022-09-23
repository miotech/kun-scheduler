package com.miotech.kun.workflow;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.annotation.Internal;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class TaskRunStateMachineBuffer {

    private final Logger logger = LoggerFactory.getLogger(TaskRunStateMachineBuffer.class);

    private final Map<Long, TaskRunStateMachine> buffer = new ConcurrentHashMap();

    private final TaskRunDao taskRunDao;

    private final ScheduledExecutorService flusher = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("stateMache-flusher" + "-%d").build());


    private volatile boolean inFlushing = false;

    @Inject
    public TaskRunStateMachineBuffer(TaskRunDao taskRunDao) {
        this.taskRunDao = taskRunDao;
        flusher.scheduleAtFixedRate(() -> batchFlush(false), 60l, 60l, TimeUnit.SECONDS);
    }

    public void put(TaskRunStateMachine stateMachine) {
        while (inFlushing) {
            //wait flush finished
            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
        }
        buffer.put(stateMachine.getTaskRunId(), stateMachine);

    }

    public TaskRunStateMachine get(Long taskRunId) {
        while (inFlushing) {
            //wait flush finished
            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
        }
        return buffer.get(taskRunId);
    }

    public List<TaskRunStateMachine> getAll(){
        return buffer.values().stream().collect(Collectors.toList());
    }

    public void remove(TaskRunStateMachine stateMachine) {
        cleanStateMachine(stateMachine);
    }

    public boolean contains(Long taskRunId) {
        return buffer.containsKey(taskRunId);
    }

    public void batchFlush(boolean force) {
        if (inFlushing && !force) {
            return;
        }
        inFlushing = true;
        try {
            logger.debug("going to flush, has {} state machine in buffer", buffer.size());
            for (Map.Entry<Long, TaskRunStateMachine> entry : buffer.entrySet()) {
                TaskRunStateMachine stateMachine = entry.getValue();
                if (stateMachine.isScheduling()) {
                    continue;
                }
                Integer taskAttemptPhase = stateMachine.getTaskAttemptPhase();
                //remove from buffer after finished
                if (TaskRunPhase.isFinished(taskAttemptPhase)) {
                    cleanStateMachine(stateMachine);
                }
            }
            logger.debug("after flush, has {} state machine in buffer", buffer.size());
        } catch (Throwable e) {
            logger.error("failed to flush taskRun machine to database", e);
        } finally {
            inFlushing = false;
        }
    }

    /**
     * just for test
     */
    @Internal
    public void reset() {
        buffer.clear();
    }

    private void cleanStateMachine(TaskRunStateMachine stateMachine) {
        buffer.remove(stateMachine.getTaskRunId());
        stateMachine.setClosed();
    }

}
