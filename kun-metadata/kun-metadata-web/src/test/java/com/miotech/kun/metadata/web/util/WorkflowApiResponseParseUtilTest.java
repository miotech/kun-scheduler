package com.miotech.kun.metadata.web.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WorkflowApiResponseParseUtilTest {

    private Operator operator;
    private List<Operator> operators = Lists.newArrayList();
    private String operatorJson;
    private String operatorsJson;

    private Task task;
    private List<Task> tasks = Lists.newArrayList();
    private String taskJson;
    private String tasksJson;
    private String errorJson;

    private String taskRunIdJson;
    private static final Long taskRunId = IdGenerator.getInstance().nextId();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        operator = Operator.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName("Test Operator")
                .withClassName("org.example.Operator")
                .build();

        operators.add(operator);

        operatorJson = toJsonString(operator);
        operatorsJson = toJsonString(operators);

        task = Task.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withOperatorId(operator.getId())
                .withName("Test Task")
                .withArguments(Lists.newArrayList())
                .withVariableDefs(Lists.newArrayList())
                .withDependencies(Lists.newArrayList())
                .build();
        tasks.add(task);
        taskJson = toJsonString(task);
        tasksJson = toJsonString(tasks);

        taskRunIdJson = toJsonString(Arrays.asList(taskRunId));

        errorJson = "{\"error\":\"Internal Server Error\",\"status\":500}";
    }

    @After
    public void tearDown() {
        operator = null;
        operators = Lists.newArrayList();
        operatorJson = null;
        operatorsJson = null;

        task = null;
        tasks = Lists.newArrayList();
        taskJson = null;
        tasksJson = null;
        errorJson = null;
    }

    @Test
    public void testParseOperatorIdAfterSearch_ok() {
        Long id = WorkflowApiResponseParseUtil.parseOperatorIdAfterSearch(operatorsJson, operator.getName());
        assertThat(id, is(operator.getId()));
    }

    @Test(expected = IllegalStateException.class)
    public void testParseOperatorIdAfterSearch_error() {
        WorkflowApiResponseParseUtil.parseOperatorIdAfterSearch(errorJson, operator.getName());
    }

    @Test
    public void testParseIdAfterCreate_ok() {
        Long id = WorkflowApiResponseParseUtil.parseIdAfterCreate(operatorJson);
        assertThat(id, is(operator.getId()));
    }

    @Test(expected = IllegalStateException.class)
    public void testParseIdAfterCreate_error() {
        WorkflowApiResponseParseUtil.parseIdAfterCreate(errorJson);
    }

    @Test
    public void testParseTaskIdAfterSearch_ok() {
        Long id = WorkflowApiResponseParseUtil.parseTaskIdAfterSearch(tasksJson, operator.getId(), task.getName());
        assertThat(id, is(task.getId()));
    }

    @Test(expected = IllegalStateException.class)
    public void testParseTaskIdAfterSearch_error() {
        WorkflowApiResponseParseUtil.parseTaskIdAfterSearch(errorJson, operator.getId(), task.getName());
    }

    @Test
    public void testJudgeOperatorExists_ok() {
        boolean exists = WorkflowApiResponseParseUtil.judgeOperatorExists(operatorsJson, operator.getName());
        assertThat(exists, is(true));
    }

    @Test
    public void testJudgeOperatorExists_error() {
        boolean exists = WorkflowApiResponseParseUtil.judgeOperatorExists(errorJson, operator.getName());
        assertThat(exists, is(false));
    }

    @Test
    public void testJudgeTaskExists_ok() {
        boolean exists = WorkflowApiResponseParseUtil.judgeTaskExists(tasksJson, operator.getId().toString(), task.getName());
        assertThat(exists, is(true));
    }

    @Test
    public void testJudgeTaskExists_error() {
        boolean exists = WorkflowApiResponseParseUtil.judgeTaskExists(errorJson, operator.getId().toString(), task.getName());
        assertThat(exists, is(false));
    }

    @Test
    public void testParseProcessId_ok() {
        String processId = WorkflowApiResponseParseUtil.parseProcessId(taskRunIdJson);
        assertThat(processId, is(taskRunId.toString()));
    }

    @Test(expected = IllegalStateException.class)
    public void testParseProcessId_error() {
        WorkflowApiResponseParseUtil.parseProcessId(errorJson);
    }

    private <T> String toJsonString(T obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }


}
