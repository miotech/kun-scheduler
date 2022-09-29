package com.miotech.kun.monitor.sla.common.dao;

import com.google.common.collect.Lists;
import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SlaDao {

    @Autowired
    @Qualifier("neo4jJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public void save(TaskDefinitionNode taskDefinitionNode) {
        Pair<String, Object[]> paramsAndValues = buildSaveSQL(taskDefinitionNode);
        jdbcTemplate.update(paramsAndValues.getLeft(), paramsAndValues.getRight());
    }

    private Pair<String, Object[]> buildSaveSQL(TaskDefinitionNode taskDefinitionNode) {
        StringBuilder sb = new StringBuilder("CREATE (td: TASK_DEFINITION {");
        List<String> params = Lists.newArrayList();
        List<Object> values = Lists.newArrayList();
        if (taskDefinitionNode.getId() != null) {
            params.add("id: ?");
            values.add(taskDefinitionNode.getId());
        }

        if (taskDefinitionNode.getName() != null) {
            params.add("name: ?");
            values.add(taskDefinitionNode.getName());
        }

        if (taskDefinitionNode.getLevel() != null) {
            params.add("level: ?");
            values.add(taskDefinitionNode.getLevel());
        }

        if (taskDefinitionNode.getDeadline() != null) {
            params.add("deadline: ?");
            values.add(taskDefinitionNode.getDeadline());
        }

        if (taskDefinitionNode.getWorkflowTaskId() != null) {
            params.add("workflowTaskId: ?");
            values.add(taskDefinitionNode.getWorkflowTaskId());
        }

        if (taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes() != null) {
            params.add("avgTaskRunTimeLastSevenTimes: ?");
            values.add(taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes());
        }

        return Pair.of(sb.append(StringUtils.join(params, ",")).append("})").toString(), values.toArray());
    }

    public void bind(Long from, Long to, TaskDefinitionNode.Relationship relationship) {
        String query = "MATCH (x: TASK_DEFINITION {id: ?}), (y: TASK_DEFINITION {id: ?}) CREATE (x) - [:%s] -> (y)";
        jdbcTemplate.update(String.format(query, relationship.toString()), from, to);
    }

    public void unbind(Long from, Long to, TaskDefinitionNode.Relationship relationship) {
        String query = "MATCH (x: TASK_DEFINITION{id:?})-[r:%s]->(y: TASK_DEFINITION{id:?}) DELETE r";
        jdbcTemplate.update(String.format(query, relationship.toString()), from, to);
    }

    public void unbind(Long to, TaskDefinitionNode.Relationship relationship) {
        String query = "MATCH (x: TASK_DEFINITION)-[r: %s]->(y: TASK_DEFINITION{id: ?}) DELETE r";
        jdbcTemplate.update(String.format(query, relationship.toString()), to);
    }

    public List<List<TaskDefinitionNode>> findDownstreamPathHasSlaConfig(Long definitionId) {
        String query = "MATCH (td1:TASK_DEFINITION{id: ?}), (td2:TASK_DEFINITION), p=shortestPath((td1)-[r: OUTPUT*]->(td2)) " +
                "WHERE td2.id <> ? and (exists(td2.deadline) or exists(td2.level)) RETURN p";
        return jdbcTemplate.query(query, rs -> {
            List<List<TaskDefinitionNode>> result = Lists.newArrayList();
            while (rs.next()) {
                List<Map> data = JSONUtils.jsonArrayToList(rs.getString("p"), Map.class);
                List<Map> nodes = data.stream().filter(node -> node.containsKey("id")).collect(Collectors.toList());
                List<TaskDefinitionNode> collect = nodes.stream().map(node -> {
                    TaskDefinitionNode taskDefinitionNode = new TaskDefinitionNode();
                    taskDefinitionNode.setId((Long) node.get("id"));
                    taskDefinitionNode.setName((String) node.get("name"));
                    taskDefinitionNode.setWorkflowTaskId((Long) node.get("workflowTaskId"));
                    if (node.containsKey("deadline")) {
                        taskDefinitionNode.setDeadline((Integer) node.get("deadline"));
                    }
                    if (node.containsKey("level")) {
                        taskDefinitionNode.setLevel((Integer) node.get("level"));
                    }
                    if (node.containsKey("avgTaskRunTimeLastSevenTimes")) {
                        taskDefinitionNode.setAvgTaskRunTimeLastSevenTimes((Integer) node.get("avgTaskRunTimeLastSevenTimes"));
                    }
                    return taskDefinitionNode;
                }).collect(Collectors.toList());
                result.add(collect);
            }

            return result;
        }, definitionId, definitionId);
    }

    public void updateAvgTaskRunTimeLastSevenTimes(Long definitionId, int avgTaskRunTimeLastSevenTimes) {
        String query = "MATCH (td: TASK_DEFINITION{id: ?}) SET td.avgTaskRunTimeLastSevenTimes = ? RETURN td";
        jdbcTemplate.update(query, definitionId, avgTaskRunTimeLastSevenTimes);
    }

    public void deleteNodeAndRelationship(Long taskDefId) {
        String query = "MATCH (td: TASK_DEFINITION{id: ?}) DETACH DELETE td";
        jdbcTemplate.update(query, taskDefId);
    }

    public TaskDefinitionNode findById(Long taskDefId) {
        String query = "MATCH (td: TASK_DEFINITION{id: ?}) RETURN td";
        return jdbcTemplate.query(query, rs -> {
            TaskDefinitionNode taskDefinitionNode = null;
            if (rs.next()) {
                taskDefinitionNode = new TaskDefinitionNode();
                Map<String, Object> nodeMap = rs.getObject("td", Map.class);
                taskDefinitionNode.setId((Long) nodeMap.get("id"));
                taskDefinitionNode.setName((String) nodeMap.get("name"));
                taskDefinitionNode.setWorkflowTaskId((Long) nodeMap.get("workflowTaskId"));
                if (nodeMap.containsKey("deadline")) {
                    taskDefinitionNode.setDeadline(((Long) nodeMap.get("deadline")).intValue());
                }
                if (nodeMap.containsKey("level")) {
                    taskDefinitionNode.setLevel(((Long) nodeMap.get("level")).intValue());
                }
                if (nodeMap.containsKey("avgTaskRunTimeLastSevenTimes")) {
                    taskDefinitionNode.setAvgTaskRunTimeLastSevenTimes(((Long) nodeMap.get("avgTaskRunTimeLastSevenTimes")).intValue());
                }
            }
            return taskDefinitionNode;
        }, taskDefId);
    }

    public void update(TaskDefinitionNode node) {
        Pair<String, Object[]> paramsAndValues = buildUpdateSQL(node);
        jdbcTemplate.update(paramsAndValues.getLeft(), paramsAndValues.getRight());
    }

    private Pair<String, Object[]> buildUpdateSQL(TaskDefinitionNode taskDefinitionNode) {
        StringBuilder sb = new StringBuilder("MATCH (td: TASK_DEFINITION{id: ?}) SET ");
        List<String> params = Lists.newArrayList();
        List<Object> values = Lists.newArrayList();
        values.add(taskDefinitionNode.getId());
        if (taskDefinitionNode.getName() != null) {
            params.add("td.name=?");
            values.add(taskDefinitionNode.getName());
        }

        if (taskDefinitionNode.getLevel() != null) {
            params.add("td.level=?");
            values.add(taskDefinitionNode.getLevel());
        } else {
            params.add("td.level=null");
        }

        if (taskDefinitionNode.getDeadline() != null) {
            params.add("td.deadline=?");
            values.add(taskDefinitionNode.getDeadline());
        } else {
            params.add("td.deadline=null");
        }

        if (taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes() != null) {
            params.add("td.avgTaskRunTimeLastSevenTimes=?");
            values.add(taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes());
        }

        if (taskDefinitionNode.getWorkflowTaskId() != null) {
            params.add("td.workflowTaskId=?");
            values.add(taskDefinitionNode.getWorkflowTaskId());
        }

        return Pair.of(sb.append(StringUtils.join(params, ",")).toString(), values.toArray());
    }
}
