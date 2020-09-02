package com.miotech.kun.dataplatform.common.deploy.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.deploy.vo.DeploySearchRequest;
import com.miotech.kun.dataplatform.model.deploy.Deploy;
import com.miotech.kun.dataplatform.mocking.MockDeployFactory;
import com.miotech.kun.workflow.client.model.PaginationResult;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO: figure out a solution to bootstrap Workflow facade related tests
@Ignore
public class DeployDaoTest extends AppTestBase {

    @Autowired
    private DeployDao deployDao;

    @Test
    public void testCreate_Deploy_ok() {
        Deploy deploy = MockDeployFactory.createDeploy();
        deployDao.create(deploy);

        Deploy fetched = deployDao.fetchById(deploy.getId()).get();
        assertThat(fetched, sameBeanAs(deploy));
    }

    @Test
    public void search() {
        List<Deploy> deployList = MockDeployFactory.createDeploy(100);
        deployList.forEach(x -> deployDao.create(x));
        Deploy deploy = deployList.get(0);
        DeploySearchRequest request;
        PaginationResult<Deploy> result;

        request = new DeploySearchRequest(10, 1,
                Collections.emptyList(),
                Optional.empty(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                Optional.empty());
        result = deployDao.search(request);
        assertThat(result.getTotalCount(), Matchers.is((long) deployList.size()));
        assertThat(result.getPageSize(), Matchers.is(10));
        assertThat(result.getPageNum(), Matchers.is(1));

        request = new DeploySearchRequest(10, 1,
                Collections.singletonList(deploy.getCreator()),
                Optional.of(deploy.getSubmittedAt()),
                Optional.of(deploy.getSubmittedAt()),
                Collections.singletonList(deploy.getDeployer()),
                Optional.of(deploy.getDeployedAt()),
                Optional.of(deploy.getDeployedAt()));
        result = deployDao.search(request);
        assertTrue(result.getTotalCount() > 0);
        assertThat(result.getPageSize(), Matchers.is(10));
        assertThat(result.getPageNum(), Matchers.is(1));
        assertThat(result.getRecords().get(0), sameBeanAs(deploy));
    }
}