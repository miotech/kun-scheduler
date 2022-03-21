package com.miotech.kun.dataplatform.web.common.commit.service;

import com.miotech.kun.dataplatform.DataPlatformTestBase;
import com.miotech.kun.dataplatform.facade.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.facade.model.commit.CommitType;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.mocking.MockTaskCommitFactory;
import com.miotech.kun.dataplatform.web.common.commit.vo.TaskCommitVO;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.security.testing.WithMockTestUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.*;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithMockTestUser
public class TaskCommitServiceTest extends DataPlatformTestBase {

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
    public void findByTaskDefinitionId_success() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskCommit taskCommit1 = taskCommitService.commit(taskDefinition.getDefinitionId(), "first time");
        TaskCommit taskCommit2 = taskCommitService.commit(taskDefinition.getDefinitionId(), "second time");

        List<TaskCommit> result = taskCommitService.findByTaskDefinitionId(taskDefinition.getDefinitionId());
        List<TaskCommit> commitList = Arrays.asList(taskCommit1, taskCommit2);
        assertThat(new HashSet<>(commitList.stream().map(TaskCommit::getId).collect(Collectors.toList())),
                is(new HashSet<>(result.stream().map(TaskCommit::getId).collect(Collectors.toList()))));
    }


    @Test
    public void convertToVO_success() {
        TaskCommit taskCommit = MockTaskCommitFactory.createTaskCommit();
        TaskCommitVO taskCommitVO = taskCommitService.convertVO(taskCommit);

        assertThat(taskCommit.getId(), is(taskCommitVO.getId()));
        assertThat(taskCommit.getDefinitionId(), is(taskCommitVO.getDefinitionId()));
        assertThat(taskCommit.getVersion(), is(taskCommitVO.getVersion()));
        assertThat(taskCommit.getMessage(), is(taskCommitVO.getMessage()));
        assertThat(taskCommit.getSnapshot(), sameBeanAs(taskCommitVO.getSnapshot()));
        assertThat(taskCommit.getCommitter(), is(taskCommitVO.getCommitter()));
        assertThat(taskCommit.getCommittedAt(), is(taskCommitVO.getCommittedAt()));
        assertThat(taskCommit.getCommitType(), is(taskCommitVO.getCommitType()));
        assertThat(taskCommit.getCommitStatus(), is(taskCommitVO.getCommitStatus()));
        assertThat(taskCommit.isLatestCommit(), is(taskCommitVO.isLatestCommit()));
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