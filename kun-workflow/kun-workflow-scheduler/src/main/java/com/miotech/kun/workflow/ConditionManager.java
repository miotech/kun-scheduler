package com.miotech.kun.workflow;

import com.miotech.kun.workflow.core.model.taskrun.ConditionStatus;
import com.miotech.kun.workflow.core.model.taskrun.ConditionType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunCondition;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ConditionManager {

    private final Logger logger = LoggerFactory.getLogger(ConditionManager.class);
    private final Map<Long, TaskRunCondition> conditionMap;
    private final Integer conditionSize;
    private final AtomicInteger satisfyCount = new AtomicInteger(0);
    private final AtomicInteger upstreamFailedCount = new AtomicInteger(0);
    private final AtomicInteger blockCount = new AtomicInteger(0);

    public ConditionManager(Map<Long, TaskRunCondition> conditionMap) {
        this.conditionMap = conditionMap;
        conditionSize = conditionMap.size();
    }

    public TaskRunCondition conditionChange(Long fromTaskRunId, Integer prePhase, Integer nextPhase) {
        TaskRunCondition taskRunCondition = conditionMap.get(fromTaskRunId);
        TaskRunCondition newCondition = updateCondition(taskRunCondition, nextPhase);
        logger.debug("update condition from {} to {} , fromTaskRunId {}", taskRunCondition, newCondition, fromTaskRunId);

        ConditionType type = taskRunCondition.getType();
        if (type.equals(ConditionType.TASKRUN_DEPENDENCY_SUCCESS)) {
            computeUpstreamFailed(prePhase, nextPhase);
        } else if (type.equals(ConditionType.TASKRUN_PREDECESSOR_FINISH)) {
            computeBlock(taskRunCondition, newCondition);
        }
        computeSatisfy(taskRunCondition, newCondition);

        conditionMap.put(fromTaskRunId, newCondition);
        return newCondition;
    }

    public TaskRunCondition initCondition(Long fromTaskRunId, Integer fromTaskRunPhase) {
        TaskRunCondition taskRunCondition = conditionMap.get(fromTaskRunId);
        TaskRunCondition newCondition = updateCondition(taskRunCondition, fromTaskRunPhase);
        //false to true
        if (newCondition.getResult()) {
            satisfyCount.incrementAndGet();
        }
        //true to false
        else if (!newCondition.getResult()) {
            ConditionType type = taskRunCondition.getType();
            if (type.equals(ConditionType.TASKRUN_PREDECESSOR_FINISH)) {
                blockCount.incrementAndGet();
            } else if (TaskRunPhase.isFailedOrUpstreamFailed(fromTaskRunPhase) && type.equals(ConditionType.TASKRUN_DEPENDENCY_SUCCESS)) {
                upstreamFailedCount.incrementAndGet();
            }
        }
        conditionMap.put(fromTaskRunId, newCondition);
        return newCondition;
    }

    public void removeCondition(Long fromTaskRunId, Integer fromTaskRunPhase) {
        conditionMap.remove(fromTaskRunId);
        if (TaskRunPhase.isFailedOrUpstreamFailed(fromTaskRunPhase)) {
            upstreamFailedCount.decrementAndGet();
        } else if (TaskRunPhase.isSuccess(fromTaskRunPhase)) {
            satisfyCount.decrementAndGet();
        }
    }

    public boolean conditionSatisfy() {
        return satisfyCount.get() == conditionSize;
    }

    public ConditionStatus resolveCondition() {

        ConditionStatus conditionStatus = ConditionStatus.WAITING;

        if (satisfyCount.get() == conditionMap.size()) {
            conditionStatus = ConditionStatus.SATISFY;
        } else if (upstreamFailedCount.get() > 0) {
            conditionStatus = ConditionStatus.UPSTREAM_FAILED;
        } else if (blockCount.get() > 0) {
            conditionStatus = ConditionStatus.BLOCK;
        }
        return conditionStatus;
    }

    private void computeSatisfy(TaskRunCondition preCondition, TaskRunCondition nextCondition) {
        if (preCondition.getResult() && !nextCondition.getResult()) {
            satisfyCount.decrementAndGet();
        } else if (!preCondition.getResult() && nextCondition.getResult()) {
            satisfyCount.incrementAndGet();
        }
    }

    private void computeUpstreamFailed(Integer prePhase, Integer nexPhase) {
        if (TaskRunPhase.isFailedOrUpstreamFailed(prePhase) && !TaskRunPhase.isFailedOrUpstreamFailed(nexPhase)) {
            upstreamFailedCount.decrementAndGet();
        } else if (!TaskRunPhase.isFailedOrUpstreamFailed(prePhase) && TaskRunPhase.isFailedOrUpstreamFailed(nexPhase)) {
            upstreamFailedCount.incrementAndGet();
        }
    }

    private void computeBlock(TaskRunCondition preCondition, TaskRunCondition nextCondition) {
        if (preCondition.getResult() && !nextCondition.getResult()) {
            blockCount.incrementAndGet();
        } else if (!preCondition.getResult() && nextCondition.getResult()) {
            blockCount.decrementAndGet();
        }
    }

    private TaskRunCondition updateCondition(TaskRunCondition taskRunCondition, Integer fromTaskRunPhase) {
        TaskRunCondition newCondition = taskRunCondition;
        if (TaskRunPhase.isSuccess(fromTaskRunPhase)) {
            newCondition = taskRunCondition.onSuccess();
        } else if (TaskRunPhase.isFailure(fromTaskRunPhase)) {
            newCondition = taskRunCondition.onFailed();
        } else if (TaskRunPhase.isFailedOrUpstreamFailed(fromTaskRunPhase)) {
            newCondition = taskRunCondition.onUpstreamFailed();
        } else if (!TaskRunPhase.isTermState(fromTaskRunPhase)) {
            newCondition = taskRunCondition.onScheduling();
        }
        return newCondition;
    }

}
