package com.miotech.kun.monitor.sla.timeline.service;

import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.monitor.sla.AppTestBase;
import com.miotech.kun.monitor.sla.common.service.SlaService;
import com.miotech.kun.monitor.sla.mocking.MockTaskDefinitionNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SlaServiceTest extends AppTestBase {

    @Autowired
    private SlaService slaService;

    @Autowired
    @Qualifier("neo4jJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

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
