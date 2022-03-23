package com.miotech.kun.dataplatform.web.common.deploy.dao;

import com.miotech.kun.dataplatform.DataPlatformTestBase;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.mocking.MockDeployedTaskFactory;
import com.miotech.kun.dataplatform.web.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.web.common.deploy.vo.DeployedTaskSearchRequest;
import com.miotech.kun.workflow.client.model.PaginationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeployedTaskDaoTest extends DataPlatformTestBase {

    @Autowired
    private DeployedTaskDao deployedTaskDao;

    @Autowired
    private TaskCommitDao taskCommitDao;

    @Test
    public void testCreate_DeployedTask_ok() {
        DeployedTask deployedTask = generateData();

        DeployedTask fetched = deployedTaskDao.fetchById(deployedTask.getDefinitionId()).get();
        assertThat(fetched, sameBeanAs(deployedTask).ignoring("taskCommit"));
        assertThat(fetched.getTaskCommit(), sameBeanAs(deployedTask.getTaskCommit()).ignoring("committedAt"));
    }

    @Test
    public void fetchByIds() {
        List<DeployedTask> deployedTasks = generateData(10);

        List<DeployedTask> fetched = deployedTaskDao.fetchByIds(
                deployedTasks.stream()
                        .map(DeployedTask::getDefinitionId)
                        .collect(Collectors.toList()));
        assertThat(fetched.size(), is(deployedTasks.size()));
    }

    @Test
    public void fetchWorkflowTaskId() {
        List<DeployedTask> deployedTasks = generateData(10);

        List<Long> fetched = deployedTaskDao.fetchWorkflowTaskId(
                deployedTasks.stream()
                        .map(DeployedTask::getDefinitionId)
                        .collect(Collectors.toList()));
        assertThat(fetched.size(), is(deployedTasks.size()));
    }

    @Test
    public void search() {
        List<DeployedTask> deployList = generateData(100);
        DeployedTask deployedTask = deployList.get(0);
        // filter nothing
        DeployedTaskSearchRequest request;
        PaginationResult<DeployedTask> deployPage;

        request = new DeployedTaskSearchRequest(10, 1,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                Collections.emptyList());
        deployPage = deployedTaskDao.search(request);
        assertThat(deployPage.getTotalCount(), is(deployList.size()));
        assertThat(deployPage.getPageSize(), is(10));
        assertThat(deployPage.getPageNum(), is(1));

        // filter with task template name
        request = new DeployedTaskSearchRequest(10, 1,
                Collections.emptyList(),
                Collections.emptyList(),
                deployedTask.getTaskTemplateName(),
                null,
                Collections.emptyList());
        deployPage = deployedTaskDao.search(request);
        assertThat(deployPage.getTotalCount(), is(deployList.size()));
        assertThat(deployPage.getPageSize(), is(10));
        assertThat(deployPage.getPageNum(), is(1));
        final DeployedTask dt = deployPage.getRecords().get(0);
        Optional<DeployedTask> deployedTaskOptional = deployList.stream().filter(d -> d.getDefinitionId().equals(dt.getDefinitionId())).findFirst();
        assertTrue(deployedTaskOptional.isPresent());
        assertThat(dt, sameBeanAs(deployedTaskOptional.get()).ignoring("taskCommit"));
        assertThat(dt.getTaskCommit(), sameBeanAs(deployedTaskOptional.get().getTaskCommit()).ignoring("committedAt"));

        // filter with name and task template name
        request = new DeployedTaskSearchRequest(10, 1,
                Collections.emptyList(),
                Collections.emptyList(),
                deployedTask.getTaskTemplateName(),
                deployedTask.getName(),
                Collections.emptyList());
        deployPage = deployedTaskDao.search(request);
        assertThat(deployPage.getTotalCount(), is(1));
        assertThat(deployPage.getPageSize(), is(10));
        assertThat(deployPage.getPageNum(), is(1));
        final DeployedTask dt2 = deployPage.getRecords().get(0);
        Optional<DeployedTask> deployedTaskOptional2 = deployList.stream().filter(d -> d.getDefinitionId().equals(dt2.getDefinitionId())).findFirst();
        assertTrue(deployedTaskOptional2.isPresent());
        assertThat(dt2, sameBeanAs(deployedTaskOptional2.get()).ignoring("taskCommit"));
        assertThat(dt2.getTaskCommit(), sameBeanAs(deployedTaskOptional2.get().getTaskCommit()).ignoring("committedAt"));

        // filter with definition id
        request = new DeployedTaskSearchRequest(10, 1,
                Collections.singletonList(deployedTask.getDefinitionId()),
                Collections.singletonList(deployedTask.getOwner()),
                deployedTask.getTaskTemplateName(),
                deployedTask.getName(),
                Collections.emptyList());
        deployPage = deployedTaskDao.search(request);
        assertThat(deployPage.getTotalCount(), is(1));
        final DeployedTask dt3 = deployPage.getRecords().get(0);
        Optional<DeployedTask> deployedTaskOptional3 = deployList.stream().filter(d -> d.getDefinitionId().equals(dt3.getDefinitionId())).findFirst();
        assertTrue(deployedTaskOptional3.isPresent());
        assertThat(dt3, sameBeanAs(deployedTaskOptional3.get()).ignoring("taskCommit"));
        assertThat(dt3.getTaskCommit(), sameBeanAs(deployedTaskOptional3.get().getTaskCommit()).ignoring("committedAt"));
    }

    @Test
    public void testFetchUnarchived() {
        List<DeployedTask> deployedTasks = generateData(2);
        List<DeployedTask> unarchived = deployedTaskDao.fetchUnarchived();
        assertThat(unarchived.size(), is(2));

        // set archived=true
        DeployedTask deployedTask = deployedTasks.get(0);
        DeployedTask archivedDeployedTask = deployedTask.cloneBuilder().withArchived(true).build();
        deployedTaskDao.update(archivedDeployedTask);

        unarchived = deployedTaskDao.fetchUnarchived();
        assertThat(unarchived.size(), is(1));
    }

    @Test
    public void testFetchByWorkflowTaskIds_empty() {
        ImmutableList<Long> emptyTaskIds = ImmutableList.of();
        Map<Long, DeployedTask> deployedTaskMap = deployedTaskDao.fetchByWorkflowTaskIds(emptyTaskIds);
        assert(deployedTaskMap.isEmpty());
    }

    @Test
    public void testFetchByWorkflowTaskIds() {
        List<DeployedTask> deployedTasks = generateData(10);

        Map<Long, DeployedTask> deployedTaskMap = deployedTaskDao.fetchByWorkflowTaskIds(
                deployedTasks.stream()
                        .map(DeployedTask::getWorkflowTaskId)
                        .collect(Collectors.toList()));
        assertThat(deployedTaskMap.size(), is(deployedTasks.size()));
    }

    private DeployedTask generateData() {
        DeployedTask deployedTask = MockDeployedTaskFactory.createDeployedTask();
        taskCommitDao.create(deployedTask.getTaskCommit());
        deployedTaskDao.create(deployedTask);
        return deployedTask;
    }

    private List<DeployedTask> generateData(int num) {
        List<DeployedTask> result = MockDeployedTaskFactory.createDeployedTask(num);
        result.forEach(x -> {
            taskCommitDao.create(x.getTaskCommit());
            deployedTaskDao.create(x);
        });
        return result;
    }
}