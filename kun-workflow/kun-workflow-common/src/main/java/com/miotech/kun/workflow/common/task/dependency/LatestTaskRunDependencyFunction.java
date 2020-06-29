package com.miotech.kun.workflow.common.task.dependency;

import com.google.common.collect.Lists;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.DependencyFunction;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class LatestTaskRunDependencyFunction implements DependencyFunction {
    private final String functionType = "latestTaskRun";

    // make this field 'transient' to avoid serialization failure of matcher sameBeanAs() in testing
    private transient final TaskRunDao taskRunDao;

    @Inject
    public LatestTaskRunDependencyFunction(TaskRunDao taskRunDao) {
        this.taskRunDao = taskRunDao;
    }

    @Override
    public List<Long> resolveDependency(Task self, Long upstreamTaskId, Tick tick, List<TaskRun> others) {
        for (TaskRun otherTaskRun : others) {
            if (otherTaskRun.getTask().getId().equals(upstreamTaskId)) {
                return Lists.newArrayList(otherTaskRun.getId());
            }
        }

        TaskRun upstreamTaskRun = taskRunDao.fetchLatestTaskRun(upstreamTaskId);
        return Lists.newArrayList(upstreamTaskRun.getId());
    }

    @Override
    public String toFunctionType() {
        return functionType;
    }
}
