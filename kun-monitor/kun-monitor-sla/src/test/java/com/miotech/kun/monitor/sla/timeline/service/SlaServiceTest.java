package com.miotech.kun.monitor.sla.timeline.service;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.facade.TaskDefinitionFacade;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.monitor.sla.MonitorSlaTestBase;
import com.miotech.kun.monitor.sla.common.dao.TaskTimelineDao;
import com.miotech.kun.monitor.sla.common.service.SlaService;
import com.miotech.kun.monitor.sla.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.monitor.sla.mocking.MockTaskDefinitionNodeFactory;
import com.miotech.kun.monitor.sla.mocking.MockTaskTimelineFactory;
import com.miotech.kun.monitor.sla.model.BacktrackingTaskDefinition;
import com.miotech.kun.monitor.sla.model.SlaBacktrackingInformation;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doReturn;

public class SlaServiceTest extends MonitorSlaTestBase {

    @Autowired
    private SlaService slaService;

    @Autowired
    @Qualifier("neo4jJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskTimelineDao taskTimelineDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @BeforeEach
    public void setUp() {
        // clear all nodes
        jdbcTemplate.update("MATCH (td: TASK_DEFINITION) DETACH DELETE td");
    }

    @Test
    public void testSave_withoutNullProperties() {
        TaskDefinitionNode taskDefinitionNode = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode);
        TaskDefinitionNode node = slaService.findById(taskDefinitionNode.getId());
        // validate
        assertThat(node, notNullValue());
        assertThat(node.getId(), is(taskDefinitionNode.getId()));
        assertThat(node.getName(), is(taskDefinitionNode.getName()));
        assertThat(node.getLevel(), is(taskDefinitionNode.getLevel()));
        assertThat(node.getDeadline(), is(taskDefinitionNode.getDeadline()));
        assertThat(node.getWorkflowTaskId(), is(taskDefinitionNode.getWorkflowTaskId()));
        assertThat(node.getAvgTaskRunTimeLastSevenTimes(), is(taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes()));

    }

    @Test
    public void testSave_withNullProperties() {
        TaskDefinitionNode taskDefinitionNode = MockTaskDefinitionNodeFactory.create();
        // set level=null
        taskDefinitionNode.setLevel(null);
        slaService.save(taskDefinitionNode);
        TaskDefinitionNode node = slaService.findById(taskDefinitionNode.getId());
        // validate
        assertThat(node, notNullValue());
        assertThat(node.getId(), is(taskDefinitionNode.getId()));
        assertThat(node.getName(), is(taskDefinitionNode.getName()));
        assertThat(node.getLevel(), nullValue());
        assertThat(node.getDeadline(), is(taskDefinitionNode.getDeadline()));
        assertThat(node.getWorkflowTaskId(), is(taskDefinitionNode.getWorkflowTaskId()));
        assertThat(node.getAvgTaskRunTimeLastSevenTimes(), is(taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes()));
    }

    @Test
    public void testBind() {
        TaskDefinitionNode taskDefinitionNode1 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode1);
        TaskDefinitionNode taskDefinitionNode2 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode2);
        // bind
        slaService.bind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT);
        // validate
        validateBind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT, 1L);
    }

    @Test
    public void testUnBind_from_to() {
        TaskDefinitionNode taskDefinitionNode1 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode1);
        TaskDefinitionNode taskDefinitionNode2 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode2);
        // bind
        slaService.bind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT);
        // validate
        validateBind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT, 1L);

        // unbind
        slaService.unbind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT);
        // validate
        validateBind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT, 0L);
    }

    @Test
    public void testUnBind_to() {
        TaskDefinitionNode taskDefinitionNode1 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode1);
        TaskDefinitionNode taskDefinitionNode2 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode2);
        // bind
        slaService.bind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT);
        // validate
        validateBind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT, 1L);

        // unbind
        slaService.unbind(taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT);
        // validate
        validateBind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT, 0L);
    }

    @Test
    public void testFindDownstreamPathHasSlaConfig() {
        TaskDefinitionNode taskDefinitionNode1 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode1);
        TaskDefinitionNode taskDefinitionNode2 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode2);
        // bind
        slaService.bind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT);

        List<List<TaskDefinitionNode>> paths = slaService.findDownstreamPathHasSlaConfig(taskDefinitionNode1.getId());
        assertThat(paths.size(), is(1));
        List<TaskDefinitionNode> path = paths.get(0);
        assertThat(path.size(), is(2));
        TaskDefinitionNode taskDefinitionNode = path.get(0);
        assertThat(path.get(0), sameBeanAs(taskDefinitionNode1));
        assertThat(path.get(1), sameBeanAs(taskDefinitionNode2));
    }

    @Test
    public void testUpdateAvgTaskRunTimeLastSevenTimes() {
        TaskDefinitionNode taskDefinitionNode = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode);
        // update runTime
        taskDefinitionNode.setAvgTaskRunTimeLastSevenTimes(taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes() + 1);
        slaService.updateAvgTaskRunTimeLastSevenTimes(taskDefinitionNode.getId(), taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes());
        // validate
        TaskDefinitionNode node = slaService.findById(taskDefinitionNode.getId());
        assertThat(node, notNullValue());
        assertThat(node, sameBeanAs(taskDefinitionNode));
    }

    @Test
    public void testDeleteNodeAndRelationship() {
        TaskDefinitionNode taskDefinitionNode1 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode1);
        TaskDefinitionNode taskDefinitionNode2 = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode2);
        // bind
        slaService.bind(taskDefinitionNode1.getId(), taskDefinitionNode2.getId(), TaskDefinitionNode.Relationship.OUTPUT);

        // delete node
        slaService.deleteNodeAndRelationship(taskDefinitionNode2.getId());
        // validate
        TaskDefinitionNode node = slaService.findById(taskDefinitionNode2.getId());
        assertThat(node, nullValue());
    }

    @Test
    public void testFindById_empty() {
        TaskDefinitionNode taskDefinitionNode = MockTaskDefinitionNodeFactory.create();
        TaskDefinitionNode node = slaService.findById(taskDefinitionNode.getId());
        assertThat(node, nullValue());
    }

    @Test
    public void testFindById_not_empty() {
        TaskDefinitionNode taskDefinitionNode = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode);
        TaskDefinitionNode node = slaService.findById(taskDefinitionNode.getId());
        assertThat(node, notNullValue());
        assertThat(node, sameBeanAs(taskDefinitionNode));
    }

    @Test
    public void testUpdate() {
        TaskDefinitionNode taskDefinitionNode = MockTaskDefinitionNodeFactory.create();
        slaService.save(taskDefinitionNode);
        // update
        taskDefinitionNode.setName(taskDefinitionNode.getName() + "_new");
        slaService.update(taskDefinitionNode);
        // validate
        TaskDefinitionNode node = slaService.findById(taskDefinitionNode.getId());
        assertThat(node, notNullValue());
        assertThat(node, sameBeanAs(taskDefinitionNode));
    }

    @Test
    public void testFindBacktrackingInformation_backtracking_exists() {
        // prepare
        Long definitionId = IdGenerator.getInstance().nextId();
        Long rootDefinitionId1 = IdGenerator.getInstance().nextId();
        Long rootDefinitionId2 = IdGenerator.getInstance().nextId();
        TaskTimeline taskTimeline = MockTaskTimelineFactory.createTaskTimeline(definitionId);
        taskTimelineDao.create(taskTimeline);
        TaskTimeline taskTimeline1 = MockTaskTimelineFactory.createTaskTimeline(definitionId, rootDefinitionId1);
        taskTimelineDao.create(taskTimeline1);
        TaskTimeline taskTimeline2 = MockTaskTimelineFactory.createTaskTimeline(definitionId, rootDefinitionId2);
        taskTimelineDao.create(taskTimeline2);

        TaskDefinitionNode taskDefinitionNode = MockTaskDefinitionNodeFactory.create(definitionId);
        slaService.save(taskDefinitionNode);

        // mock
        TaskDefinition taskDefinition1 = MockTaskDefinitionFactory.createTaskDefinition(rootDefinitionId1);
        TaskDefinition taskDefinition2 = MockTaskDefinitionFactory.createTaskDefinition(rootDefinitionId2);
        taskDefinitionDao.create(taskDefinition1);
        taskDefinitionDao.create(taskDefinition2);

        SlaBacktrackingInformation backtrackingInformation = slaService.findBacktrackingInformation(definitionId);
        assertThat(backtrackingInformation.getAvgTaskRunTimeLastSevenTimes(), is(taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes()));
        Long taskDefId = backtrackingInformation.getBacktrackingTaskDefinition().getDefinitionId();
        assertThat(taskDefId, is(rootDefinitionId2));
    }

    @Test
    public void testFindBacktrackingInformation_backtracking_not_exists() {
        // prepare
        Long definitionId = IdGenerator.getInstance().nextId();
        TaskTimeline taskTimeline = MockTaskTimelineFactory.createTaskTimeline(definitionId);
        taskTimelineDao.create(taskTimeline);

        TaskDefinitionNode taskDefinitionNode = MockTaskDefinitionNodeFactory.create(definitionId);
        slaService.save(taskDefinitionNode);

        // mock
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition(definitionId);
        taskDefinitionDao.create(taskDefinition);

        SlaBacktrackingInformation backtrackingInformation = slaService.findBacktrackingInformation(definitionId);
        assertThat(backtrackingInformation.getAvgTaskRunTimeLastSevenTimes(), is(taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes()));
        assertThat(backtrackingInformation.getBacktrackingTaskDefinition(), nullValue());
    }

    private void validateBind(Long from, Long to, TaskDefinitionNode.Relationship relationship, Long expectedCount) {
        String relationshipCount = "MATCH (n: TASK_DEFINITION{id: ?})-[r:%s]->(m: TASK_DEFINITION{id: ?}) return count(1) as c";
        Long count = jdbcTemplate.query(String.format(relationshipCount, relationship.toString()), rs -> {
            if (rs.next()) {
                return rs.getLong("c");
            }
            return null;
        }, from, to);

        assertThat(count, is(expectedCount));
    }

}
