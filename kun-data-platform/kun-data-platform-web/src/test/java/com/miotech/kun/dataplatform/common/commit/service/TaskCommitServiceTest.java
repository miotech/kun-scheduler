package com.miotech.kun.dataplatform.common.commit.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.model.commit.CommitType;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.security.testing.WithMockTestUser;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@WithMockTestUser
public class TaskCommitServiceTest extends AppTestBase {

    @Autowired
    private TaskCommitService taskCommitService;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Test
    public void commit_failed() {
        try {
            taskCommitService.commit(1L, null);
        } catch (Throwable e) {
            assertTrue(NoSuchElementException.class.isAssignableFrom(e.getClass()));
            assertThat(e.getMessage(), Matchers.is("Task definition not found: \"1\""));
        }
    }

    @Test
    public void commit_ok() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);

        String msg = "Create test message";
        TaskCommit commit = taskCommitService.commit(taskDefinition.getDefinitionId(), msg);
        assertThat(commit.getCommitStatus(), Matchers.is(CommitStatus.SUBMITTED));
        assertThat(commit.getCommitType(), Matchers.is(CommitType.CREATED));
        assertThat(commit.getVersion(), Matchers.is("V1"));
        assertThat(commit.getMessage(), Matchers.is(msg));
    }
}