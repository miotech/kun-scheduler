package com.miotech.kun.common.taskrun.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.vo.TaskRunVO;
import java.util.List;

@Singleton
public class TaskRunService {

    @Inject
    private TaskRunDao taskRunDao;


    public TaskRunVO getTaskRunDetail(Long taskRunId) {
        TaskRun taskRun = taskRunDao.fetchById(taskRunId);
        return convertToVO(taskRun);
    }

    public TaskRunVO convertToVO(TaskRun taskRun) {
        List<TaskAttempt> attempts = taskRunDao.fetchAttemptsByTaskRunId(taskRun.getId());

        return TaskRunVO.newBuilder()
                .withTask(taskRun.getTask())
                .withId(taskRun.getId())
                .withScheduledTick(taskRun.getScheduledTick())
                .withStatus(taskRun.getStatus())
                .withInlets(taskRun.getInlets())
                .withOutlets(taskRun.getOutlets())
                .withDependencyTaskRunIds(taskRun.getDependencies())
                .withStartAt(taskRun.getStartAt())
                .withEndAt(taskRun.getEndAt())
                .withAttempts(attempts)
                .build();
    }
}
