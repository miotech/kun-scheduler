package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.facade.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.facade.model.commit.CommitType;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.commit.TaskSnapshot;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockTaskCommitFactory {

    private MockTaskCommitFactory() {}

    public static TaskCommit createTaskCommit() {
        return createTaskCommit(1).get(0);
    }

    public static List<TaskCommit> createTaskCommit(int num) {
        List<TaskCommit> taskCommits = new ArrayList<>();
        Long lastTasksDefId = null;
        for (int i = 0; i < num; i++) {
            long commitId = DataPlatformIdGenerator.nextCommitId();
            TaskDefinition taskDefinition;
            if (lastTasksDefId == null) {
                taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
            } else {
                taskDefinition = MockTaskDefinitionFactory.createTaskDefinitions(1,
                        Collections.singletonList(lastTasksDefId)).get(0);
            }
            long definitionId = taskDefinition.getDefinitionId();
            lastTasksDefId = definitionId;
            taskCommits.add(TaskCommit.newBuilder()
                    .withId(commitId)
                    .withDefinitionId(definitionId)
                    .withMessage("test commit")
                    .withSnapshot(TaskSnapshot.newBuilder()
                            .withName(taskDefinition.getName())
                            .withTaskPayload(taskDefinition.getTaskPayload())
                            .withTaskTemplateName(taskDefinition.getTaskTemplateName())
                            .withOwner(1L)
                            .build())
                    .withCommitter(1L)
                    .withVersion("V1")
                    .withCommittedAt(OffsetDateTime.now())
                    .withCommitType(CommitType.CREATED)
                    .withCommitStatus(CommitStatus.SUBMITTED)
                    .withLatestCommit(true)
                    .build()
            );
        }
        return taskCommits;
    }
}
