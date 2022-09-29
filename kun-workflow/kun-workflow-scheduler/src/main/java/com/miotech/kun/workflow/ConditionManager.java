package com.miotech.kun.workflow;

import com.miotech.kun.workflow.core.model.taskrun.ConditionStatus;
import com.miotech.kun.workflow.core.model.taskrun.ConditionType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunCondition;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * manage taskRunStateMachine's condition
 */
public class ConditionManager {

    private final Logger logger = LoggerFactory.getLogger(ConditionManager.class);
    private final Map<Long, TaskRunCondition> conditionMap;
    private final Map<Long, Integer> conditionPhase;
    private Integer satisfyCount = 0;
    private Integer upstreamFailedCount = 0;
    private Integer blockCount = 0;

    public ConditionManager(Map<Long, TaskRunCondition> conditionMap) {
        this.conditionMap = conditionMap;
        conditionPhase = new HashMap<>();
    }

    public TaskRunCondition conditionChange(Long fromTaskRunId, Integer prePhase, Integer nextPhase) {
        conditionPhase.put(fromTaskRunId, nextPhase);
        TaskRunCondition taskRunCondition = conditionMap.get(fromTaskRunId);
        TaskRunCondition newCondition = updateCondition(taskRunCondition, nextPhase);
        conditionMap.put(fromTaskRunId, newCondition);
        logger.debug("update condition from {} to {} , fromTaskRunId {}", taskRunCondition, newCondition, fromTaskRunId);

        ConditionType type = taskRunCondition.getType();
        if (type.equals(ConditionType.TASKRUN_DEPENDENCY_SUCCESS)) {
            computeUpstreamFailed();
        } else if (type.equals(ConditionType.TASKRUN_PREDECESSOR_FINISH)) {
            computeBlock();
        }
        computeSatisfy();

        return newCondition;
    }

    public TaskRunCondition initCondition(Long fromTaskRunId, Integer fromTaskRunPhase) {
        conditionPhase.put(fromTaskRunId, fromTaskRunPhase);
        TaskRunCondition taskRunCondition = conditionMap.get(fromTaskRunId);
        TaskRunCondition newCondition = updateCondition(taskRunCondition, fromTaskRunPhase);
        conditionMap.put(fromTaskRunId, newCondition);

        if (newCondition.getResult()) {
            computeSatisfy();
        } else if (!newCondition.getResult()) {
            ConditionType type = taskRunCondition.getType();
            if (type.equals(ConditionType.TASKRUN_PREDECESSOR_FINISH)) {
                computeBlock();
            } else if (TaskRunPhase.isFailedOrUpstreamFailed(fromTaskRunPhase) && type.equals(ConditionType.TASKRUN_DEPENDENCY_SUCCESS)) {
                computeUpstreamFailed();
            }
        }
        return newCondition;
    }

    public void removeCondition(Long fromTaskRunId, Integer fromTaskRunPhase) {
        conditionMap.remove(fromTaskRunId);
        conditionPhase.remove(fromTaskRunId);
        computeBlock();
        computeUpstreamFailed();
        computeSatisfy();
    }

    public boolean conditionSatisfy() {
        return satisfyCount == conditionMap.size();
    }

    public ConditionStatus resolveCondition() {

        ConditionStatus conditionStatus = ConditionStatus.WAITING;

        if (satisfyCount == conditionMap.size()) {
            conditionStatus = ConditionStatus.SATISFY;
        } else if (upstreamFailedCount > 0) {
            conditionStatus = ConditionStatus.UPSTREAM_FAILED;
        } else if (blockCount > 0) {
            conditionStatus = ConditionStatus.BLOCK;
        }
        return conditionStatus;
    }

    private void computeSatisfy() {
        Integer currentSatisfy = 0;
        for (TaskRunCondition taskRunCondition : conditionMap.values()) {
            if (taskRunCondition.getResult()) {
                currentSatisfy++;
            }
        }
        satisfyCount = currentSatisfy;
    }

    private void computeUpstreamFailed() {
        Integer currentUpstreamFailed = 0;
        for (Long conditionTaskRunId : conditionMap.keySet()) {
            TaskRunCondition taskRunCondition = conditionMap.get(conditionTaskRunId);
            if (taskRunCondition.getType().equals(ConditionType.TASKRUN_DEPENDENCY_SUCCESS) && !taskRunCondition.getResult()
                    && TaskRunPhase.isFailedOrUpstreamFailed(conditionPhase.get(conditionTaskRunId))) {
                currentUpstreamFailed++;
            }
        }
        upstreamFailedCount = currentUpstreamFailed;
    }

    private void computeBlock() {
        Integer currentBlock = 0;
        for (TaskRunCondition taskRunCondition : conditionMap.values()) {
            if (taskRunCondition.getType().equals(ConditionType.TASKRUN_PREDECESSOR_FINISH) && !taskRunCondition.getResult()) {
                currentBlock++;
            }
        }
        blockCount = currentBlock;
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
