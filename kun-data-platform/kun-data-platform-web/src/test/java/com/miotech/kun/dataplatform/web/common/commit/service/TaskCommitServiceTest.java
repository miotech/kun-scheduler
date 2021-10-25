package com.miotech.kun.dataplatform.web.common.commit.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.facade.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.facade.model.commit.CommitType;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.security.testing.WithMockTestUser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

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
            assertThat(e.getMessage(), is("Task definition not found: \"1\""));
        }
    }

    @Test
    public void commit_ok() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);

        String msg = "Create test message";
        TaskCommit commit = taskCommitService.commit(taskDefinition.getDefinitionId(), msg);
        assertThat(commit.getCommitStatus(), is(CommitStatus.SUBMITTED));
        assertThat(commit.getCommitType(), is(CommitType.CREATED));
        assertThat(commit.getVersion(), is("V1"));
        assertThat(commit.getMessage(), is(msg));
    }

    @Test
    public void testGetLatestCommit_empty() {
        Map<Long, TaskCommit> latestCommit = taskCommitService.getLatestCommit(ImmutableList.of(1L, 2L));
        assertTrue(latestCommit.isEmpty());
    }

    @Test
    public void testGetLatestCommit_createThenFetch() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);

        String msg = "Create test message";
        taskCommitService.commit(taskDefinition.getDefinitionId(), msg);
        msg = "Create taset message v2";
        taskCommitService.commit(taskDefinition.getDefinitionId(), msg);

        Map<Long, TaskCommit> latestCommit = taskCommitService.getLatestCommit(ImmutableList.of(taskDefinition.getDefinitionId()));
        assertFalse(latestCommit.isEmpty());
        assertThat(latestCommit.get(taskDefinition.getDefinitionId()).getMessage(), is(msg));
    }

}