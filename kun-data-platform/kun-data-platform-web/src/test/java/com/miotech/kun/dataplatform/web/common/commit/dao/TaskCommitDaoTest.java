package com.miotech.kun.dataplatform.web.common.commit.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.web.common.commit.service.TaskCommitService;
import com.miotech.kun.dataplatform.web.common.commit.vo.CommitSearchRequest;
import com.miotech.kun.dataplatform.mocking.MockTaskCommitFactory;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.security.testing.WithMockTestUser;
import com.miotech.kun.workflow.client.model.PaginationResult;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@WithMockTestUser
public class TaskCommitDaoTest extends AppTestBase {

    @Autowired
    private TaskCommitDao taskCommitDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskCommitService taskCommitService;

    @Test
    public void testCreate_TaskCommit_ok() {
        TaskCommit taskCommit = MockTaskCommitFactory.createTaskCommit();
        taskCommitDao.create(taskCommit);

        TaskCommit fetched = taskCommitDao.fetchById(taskCommit.getId()).get();
        assertThat(fetched.getSnapshot(), sameBeanAs(taskCommit.getSnapshot()));
        assertThat(fetched.getDefinitionId(), is(taskCommit.getDefinitionId()));
        assertThat(fetched.getCommitType(), is(taskCommit.getCommitType()));
        assertThat(fetched.getCommitStatus(), is(taskCommit.getCommitStatus()));
        assertThat(fetched.getVersion(), is(taskCommit.getVersion()));
        assertThat(fetched.getCommitter(), is(taskCommit.getCommitter()));
        assertThat(fetched.getMessage(), is(taskCommit.getMessage()));
        assertTrue(fetched.isLatestCommit());
    }


    @Test
    public void search() {
        List<TaskCommit> commitList = MockTaskCommitFactory.createTaskCommit(100);
        commitList.forEach(x -> taskCommitDao.create(x));
        CommitSearchRequest request = new CommitSearchRequest(10, 1,
                Collections.singletonList(commitList.get(0).getId()),
                Collections.emptyList(),
                Optional.of(true));
        PaginationResult<TaskCommit> taskCommitPage = taskCommitDao.search(request);
        assertThat(taskCommitPage.getTotalCount(), Matchers.is(1));
        assertThat(taskCommitPage.getPageSize(), Matchers.is(10));
        assertThat(taskCommitPage.getPageNum(), Matchers.is(1));
    }

    @Test
    public void fetchById_success() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        TaskCommit taskCommit = MockTaskCommitFactory.createTaskCommit();
        taskCommit.cloneBuilder().withDefinitionId(taskDefinition.getDefinitionId()).build();
        taskCommitDao.create(taskCommit);
        TaskCommit result = taskCommitDao.fetchById(taskCommit.getId()).get();
        assertThat(taskCommit.getId(), is(result.getId()));
    }

    @Test
    public void fetchByTaskDefinitionId_success() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        List<TaskCommit> commitList = MockTaskCommitFactory.createTaskCommit(3);
        TaskCommit taskCommit1 = commitList.get(0).cloneBuilder()
                .withDefinitionId(taskDefinition.getDefinitionId())
                .withLatestCommit(false)
                .build();
        TaskCommit taskCommit2 = commitList.get(1).cloneBuilder()
                .withDefinitionId(taskDefinition.getDefinitionId())
                .withLatestCommit(false)
                .build();
        TaskCommit taskCommit3 = commitList.get(2).cloneBuilder()
                .withDefinitionId(taskDefinition.getDefinitionId())
                .build();
        taskCommitDao.create(taskCommit1);
        taskCommitDao.create(taskCommit2);
        taskCommitDao.create(taskCommit3);

        List<TaskCommit> result = taskCommitDao.fetchByTaskDefinitionId(taskDefinition.getDefinitionId());
        commitList = Arrays.asList(taskCommit1, taskCommit2, taskCommit3);
        assertThat(new HashSet<>(commitList.stream().map(TaskCommit::getId).collect(Collectors.toList())),
                is(new HashSet<>(result.stream().map(TaskCommit::getId).collect(Collectors.toList()))));
    }

    @Test
    public void testGetLatestCommit_definitionIdsIsEmpty() {
        Map<Long, TaskCommit> latestCommit = taskCommitDao.getLatestCommit(ImmutableList.of());
        assertTrue(latestCommit.isEmpty());
    }

    @Test
    public void testGetLatestCommit_empty() {
        Map<Long, TaskCommit> latestCommit = taskCommitDao.getLatestCommit(ImmutableList.of(1L, 2L));
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

        Map<Long, TaskCommit> latestCommit = taskCommitDao.getLatestCommit(ImmutableList.of(taskDefinition.getDefinitionId()));
        assertFalse(latestCommit.isEmpty());
        assertThat(latestCommit.get(taskDefinition.getDefinitionId()).getMessage(), is(msg));
    }

}