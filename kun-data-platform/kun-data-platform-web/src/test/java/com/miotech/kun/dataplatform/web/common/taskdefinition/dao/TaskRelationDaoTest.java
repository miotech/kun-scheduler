package com.miotech.kun.dataplatform.web.common.taskdefinition.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskRelation;
import com.miotech.kun.dataplatform.mocking.MockTaskRelationFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskRelationDaoTest extends AppTestBase {

    @Autowired
    private TaskRelationDao taskRelationDao;


    @Test
    public void testCreateAndFetch() {
        List<TaskRelation> taskRelations = MockTaskRelationFactory.createTaskRelations(1);
        taskRelationDao.create(taskRelations);

        List<TaskRelation> fetchedUpstreamRelations = taskRelationDao.fetchByUpstreamId(taskRelations.get(0).getUpstreamId());
        assertThat(fetchedUpstreamRelations.get(0), sameBeanAs(taskRelations.get(0)));

        List<TaskRelation> fetchedDownstreamRelations = taskRelationDao.fetchByDownstreamId(taskRelations.get(0).getDownstreamId());
        assertThat(fetchedDownstreamRelations.get(0), sameBeanAs(taskRelations.get(0)));
    }

    @Test
    public void testDelete() {
        List<TaskRelation> taskRelations = MockTaskRelationFactory.createTaskRelations(1);
        taskRelationDao.create(taskRelations);

        List<TaskRelation> fetchedUpstreamRelations = taskRelationDao.fetchByUpstreamId(taskRelations.get(0).getUpstreamId());
        assertEquals(1, fetchedUpstreamRelations.size());

        taskRelationDao.delete(taskRelations.get(0).getDownstreamId());
        List<TaskRelation> fetchedUpstreamRelations2 = taskRelationDao.fetchByUpstreamId(taskRelations.get(0).getUpstreamId());
        assertTrue(fetchedUpstreamRelations2.isEmpty());
    }

    @Test
    public void testFetchAll() {
        List<TaskRelation> taskRelations = MockTaskRelationFactory.createTaskRelations(1);
        taskRelationDao.create(taskRelations);

        List<TaskRelation> all = taskRelationDao.fetchAll();
        assertThat(all.size(), is(1));
        TaskRelation taskRelation = all.get(0);
        assertThat(taskRelation, sameBeanAs(taskRelations.get(0)));
    }

}