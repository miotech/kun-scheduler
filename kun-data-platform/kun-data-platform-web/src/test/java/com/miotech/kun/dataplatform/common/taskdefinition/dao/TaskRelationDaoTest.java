package com.miotech.kun.dataplatform.common.taskdefinition.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskRelation;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import com.miotech.kun.dataplatform.mocking.MockTaskRelationFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

class TaskRelationDaoTest extends AppTestBase {

    @Autowired
    private TaskRelationDao taskRelationDao;


    @Test
    public void createAndFetch() {
        List<TaskRelation> taskRelations = MockTaskRelationFactory.createTaskRelations(1);
        taskRelationDao.create(taskRelations);

        List<TaskRelation> fetchedUpstreamRelations = taskRelationDao.fetchByUpstreamId(taskRelations.get(0).getUpstreamId());
        assertThat(fetchedUpstreamRelations.get(0), sameBeanAs(taskRelations.get(0)));

        List<TaskRelation> fetchedDownstreamRelations = taskRelationDao.fetchByDownstreamId(taskRelations.get(0).getDownstreamId());
        assertThat(fetchedDownstreamRelations.get(0), sameBeanAs(taskRelations.get(0)));
    }

    @Test
    void delete() {
        List<TaskRelation> taskRelations = MockTaskRelationFactory.createTaskRelations(1);
        taskRelationDao.create(taskRelations);

        List<TaskRelation> fetchedUpstreamRelations = taskRelationDao.fetchByUpstreamId(taskRelations.get(0).getUpstreamId());
        assertEquals(1, fetchedUpstreamRelations.size());

        taskRelationDao.delete(taskRelations.get(0).getDownstreamId());
        List<TaskRelation> fetchedUpstreamRelations2 = taskRelationDao.fetchByUpstreamId(taskRelations.get(0).getUpstreamId());
        assertTrue(fetchedUpstreamRelations2.isEmpty());
    }
}