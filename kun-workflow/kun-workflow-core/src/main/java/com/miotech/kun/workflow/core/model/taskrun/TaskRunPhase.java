package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.commons.utils.ExceptionUtils;

/**
 * Represents the phase of the TaskRun,
 * the lower 16 bits representing the incomplete state
 * and the higher 16 bits representing the completed state
 */
public class TaskRunPhase {

    public static final int FINISH_OFFSET = 16;
    public static final Integer CREATED = 1;
    public static final Integer WAITING = 2;
    public static final Integer QUEUED = 4;
    public static final Integer RUNNING = 8;
    public static final Integer CHECKING = 16;
    public static final Integer SUCCESS = 1 << FINISH_OFFSET;
    public static final Integer FAILED = 2 << FINISH_OFFSET;
    public static final Integer ABORTED = 4 << FINISH_OFFSET;
    public static final Integer CHECK_FAILED = 8 << FINISH_OFFSET;
    public static final Integer ERROR = 16 << FINISH_OFFSET;
    public static final Integer SKIP = 32 << FINISH_OFFSET;
    public static final Integer UPSTREAM_FAILED = 64 << FINISH_OFFSET;
    public static final Integer BLOCKED = 128 << FINISH_OFFSET;


    /**
     * return final phase of taskRun
     *
     * @param taskRunPhase sum of all phase
     * @return
     */
    public static Integer getCurrentPhase(Integer taskRunPhase) {
        if (isFinished(taskRunPhase)) {
            return getFinishedPhase(taskRunPhase);
        }
        return getUnFinishedPhase(taskRunPhase);
    }

    /**
     * @param taskRunPhase sum of all phase
     * @return
     */
    public static boolean isFinished(Integer taskRunPhase) {
        if (taskRunPhase >> 16 > 0) {
            return true;
        }
        return false;
    }

    /**
     * @param taskRunPhase
     * @return
     */
    public static boolean isSuccess(Integer taskRunPhase) {
        if ((taskRunPhase & (SUCCESS | SKIP)) != 0) {
            return true;
        }
        return false;
    }

    public static boolean isCreated(Integer taskRunPhase) {
        if ((taskRunPhase & CREATED) != 0) {
            return true;
        }
        return false;
    }

    public static boolean isFailure(Integer taskRunPhase) {
        if ((taskRunPhase & (FAILED | ABORTED | CHECK_FAILED)) != 0) {
            return true;
        }
        return false;
    }

    public static boolean isFailedOrUpstreamFailed(Integer taskRunPhase) {
        if ((taskRunPhase & UPSTREAM_FAILED) != 0 || isFailure(taskRunPhase)) {
            return true;
        }
        return false;
    }

    public static boolean isBlocked(Integer taskRunPhase) {
        if ((taskRunPhase & BLOCKED) != 0) {
            return true;
        }
        return false;
    }

    public static boolean isUpstreamFailed(Integer taskRunPhase) {
        if ((taskRunPhase & UPSTREAM_FAILED) != 0) {
            return true;
        }
        return false;
    }

    public static boolean isTermState(Integer taskRunPhase) {
        return isFailedOrUpstreamFailed(taskRunPhase) || isFailure(taskRunPhase) || isSuccess(taskRunPhase);
    }

    public static boolean isSkipped(Integer taskRunPhase) {
        if ((taskRunPhase & SKIP) != 0) {
            return true;
        }
        return false;
    }

    /**
     * covert to TaskRunStatus
     *
     * @param taskRunPhase
     * @return
     */
    public static TaskRunStatus toStatus(Integer taskRunPhase) {
        Integer currentPhase = getCurrentPhase(taskRunPhase);
        TaskRunStatus taskRunStatus = null;
        switch (currentPhase) {
            case 1:
            case 2:
                taskRunStatus = TaskRunStatus.CREATED;
                break;
            case 4:
                taskRunStatus = TaskRunStatus.QUEUED;
                break;
            case 8:
                taskRunStatus = TaskRunStatus.RUNNING;
                break;
            case 16:
                taskRunStatus = TaskRunStatus.CHECK;
                break;
            case 1 << FINISH_OFFSET:
                taskRunStatus = TaskRunStatus.SUCCESS;
                break;
            case 2 << FINISH_OFFSET:
                taskRunStatus = TaskRunStatus.FAILED;
                break;
            case 4 << FINISH_OFFSET:
                taskRunStatus = TaskRunStatus.ABORTED;
                break;
            case 8 << FINISH_OFFSET:
                taskRunStatus = TaskRunStatus.CHECK_FAILED;
                break;
            case 16 << FINISH_OFFSET:
                taskRunStatus = TaskRunStatus.ERROR;
                break;
            case 32 << FINISH_OFFSET:
                taskRunStatus = TaskRunStatus.SKIPPED;
                break;
            case 64 << FINISH_OFFSET:
                taskRunStatus = TaskRunStatus.UPSTREAM_FAILED;
                break;
            case 128 << FINISH_OFFSET:
                taskRunStatus = TaskRunStatus.BLOCKED;
                break;
            default:
                ExceptionUtils.wrapIfChecked(new IllegalStateException("can not covert taskRunPhase : " + taskRunPhase + " to taskRunStatus"));
        }
        return taskRunStatus;
    }


    private static Integer getFinishedPhase(Integer taskRunPhase) {
        //skip is a special phase which change from finished to finished
        if (isSkipped(taskRunPhase)) {
            return SKIP;
        }
        Integer finishedPhase = 1 << FINISH_OFFSET;
        Integer lastPhase = 1 << 30;
        while (finishedPhase <= lastPhase) {
            if ((taskRunPhase & finishedPhase) != 0) {
                break;
            }
            finishedPhase = finishedPhase << 1;
        }
        return finishedPhase;
    }

    private static Integer getUnFinishedPhase(Integer taskRunPhase) {
        Integer unfinishedPhase = 1;
        Integer lastPhase = 1 << 15;

        while (unfinishedPhase <= lastPhase) {
            if ((taskRunPhase & unfinishedPhase) == 0) {
                break;
            }
            unfinishedPhase = unfinishedPhase << 1;
        }
        return unfinishedPhase >> 1;
    }

}
