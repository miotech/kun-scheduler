package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.deploy.Deploy;
import com.miotech.kun.dataplatform.model.deploy.DeployCommit;
import com.miotech.kun.dataplatform.model.deploy.DeployStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockDeployFactory {


    private MockDeployFactory() {}

    public static Deploy createDeploy() {
        return createDeploy(1).get(0);
    }

    public static List<Deploy> createDeploy(int num) {
        List<Deploy> deploys = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            TaskCommit taskCommit = MockTaskCommitFactory.createTaskCommit();
            long deployId = DataPlatformIdGenerator.nextDeployId();
            DeployCommit deployCommit = DeployCommit.newBuilder()
                    .withDeployId(deployId)
                    .withCommit(taskCommit.getId())
                    .withDeployStatus(DeployStatus.CREATED)
                    .build();
            deploys.add(Deploy.newBuilder()
                    .withId(deployId)
                    .withName(taskCommit.getSnapshot().getName())
                    .withCreator(1L)
                    .withSubmittedAt(DateTimeUtils.now())
                    .withDeployer(1L)
                    .withDeployedAt(DateTimeUtils.now())
                    .withCommits(Collections.singletonList(deployCommit))
                    .withStatus(DeployStatus.CREATED)
                    .build());
        }
        return deploys;
    }
}
