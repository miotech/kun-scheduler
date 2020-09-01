package com.miotech.kun.dataplatform.common.commit.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.commit.vo.CommitSearchRequest;
import com.miotech.kun.dataplatform.mocking.MockTaskCommitFactory;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.workflow.client.model.PaginationResult;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

// TODO: figure out a solution to bootstrap Workflow facade related tests
@Ignore
public class TaskCommitDaoTest extends AppTestBase {

    @Autowired
    private TaskCommitDao taskCommitDao;

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
        assertThat(taskCommitPage.getTotalCount(), Matchers.is(1L));
        assertThat(taskCommitPage.getPageSize(), Matchers.is(10));
        assertThat(taskCommitPage.getPageNum(), Matchers.is(1));
    }
}