package com.miotech.kun.dataplatform.common.deploy.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.common.deploy.vo.DeployedTaskSearchRequest;
import com.miotech.kun.dataplatform.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.mocking.MockDeployedTaskFactory;
import com.miotech.kun.workflow.client.model.PaginationResult;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DeployedTaskDaoTest extends AppTestBase {

    @Autowired
    private DeployedTaskDao deployedTaskDao;

    @Autowired
    private TaskCommitDao taskCommitDao;

    @Test
    public void testCreate_DeployedTask_ok() {
        DeployedTask deployedTask = generateData();

        DeployedTask fetched = deployedTaskDao.fetchById(deployedTask.getDefinitionId()).get();
        assertThat(fetched, sameBeanAs(deployedTask));
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
                null);
        deployPage = deployedTaskDao.search(request);
        assertThat(deployPage.getTotalCount(), is((long) deployList.size()));
        assertThat(deployPage.getPageSize(), is(10));
        assertThat(deployPage.getPageNum(), is(1));

        // filter with task template name
        request = new DeployedTaskSearchRequest(10, 1,
                Collections.emptyList(),
                Collections.emptyList(),
                deployedTask.getTaskTemplateName(),
                null);
        deployPage = deployedTaskDao.search(request);
        assertThat(deployPage.getTotalCount(), is((long) deployList.size()));
        assertThat(deployPage.getPageSize(), is(10));
        assertThat(deployPage.getPageNum(), is(1));
        assertThat(deployPage.getRecords().get(0),sameBeanAs(deployedTask));

        // filter with name and task template name
        request = new DeployedTaskSearchRequest(10, 1,
                Collections.emptyList(),
                Collections.emptyList(),
                deployedTask.getTaskTemplateName(),
                deployedTask.getName());
        deployPage = deployedTaskDao.search(request);
        assertThat(deployPage.getTotalCount(), is(1L));
        assertThat(deployPage.getPageSize(), is(10));
        assertThat(deployPage.getPageNum(), is(1));
        assertThat(deployPage.getRecords().get(0),sameBeanAs(deployedTask));

        // filter with definition id
        request = new DeployedTaskSearchRequest(10, 1,
                Collections.singletonList(deployedTask.getDefinitionId()),
                Collections.singletonList(deployedTask.getOwner()),
                deployedTask.getTaskTemplateName(),
                deployedTask.getName());
        deployPage = deployedTaskDao.search(request);
        assertThat(deployPage.getTotalCount(), is(1L));
        assertThat(deployPage.getRecords().get(0), sameBeanAs(deployedTask));
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