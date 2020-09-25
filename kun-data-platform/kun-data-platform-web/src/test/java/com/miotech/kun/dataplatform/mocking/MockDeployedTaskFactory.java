package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.deploy.DeployedTask;

import java.util.ArrayList;
import java.util.List;

public class MockDeployedTaskFactory {


    private MockDeployedTaskFactory() {}

    public static DeployedTask createDeployedTask() {
        return createDeployedTask(1).get(0);
    }

    public static List<DeployedTask> createDeployedTask(int num) {
        List<DeployedTask> tasksDefs = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            TaskCommit taskCommit = MockTaskCommitFactory.createTaskCommit();
            long taskId = DataPlatformIdGenerator.nextDeployedTaskId();
            tasksDefs.add(DeployedTask.newBuilder()
                    .withId(taskId)
                    .withName(taskCommit.getSnapshot().getName())
                    .withDefinitionId(taskCommit.getDefinitionId())
                    .withTaskTemplateName("SparkSQL")
                    .withTaskCommit(taskCommit)
                    .withWorkflowTaskId(1L)
                    .withOwner(1L)
                    .withArchived(false)
                    .build());
        }
        return tasksDefs;
    }
}
