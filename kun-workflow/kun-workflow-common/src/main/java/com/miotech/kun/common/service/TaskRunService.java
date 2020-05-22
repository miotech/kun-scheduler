package com.miotech.kun.common.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.common.dao.TaskAttemptDao;
import com.miotech.kun.common.dao.TaskDao;
import com.miotech.kun.common.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.vo.TaskRunVO;

import java.util.List;

@Singleton
public class TaskRunService {

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskAttemptDao taskAttemptDao;

    public TaskRunVO getTaskRunDetail(Long taskRunId) {

        TaskRun taskRun = taskRunDao.findByID(taskRunId);
        List<TaskAttempt> attempts = taskAttemptDao.findByTaskRunID(taskRunId);

        return TaskRunVO.newBuilder()
                .withAttempts(attempts)
                .build();
    }


}
