package com.miotech.kun.workflow.web.serializer;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.util.Types;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.common.task.vo.RunTaskVO;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class JsonSerializerTest {

    private JsonSerializer jsonSerializer;

    private String runTaskJson;

    private String runTasksJson;

    @Before
    public void setUp() {
        jsonSerializer = Guice.createInjector().getInstance(JsonSerializer.class);

        RunTaskVO runTaskVO = new RunTaskVO();
        runTaskVO.setTaskId(IdGenerator.getInstance().nextId());
        runTaskVO.setConfig(Maps.newHashMap());

        runTaskJson = JSONUtils.toJsonString(runTaskVO);
        runTasksJson = JSONUtils.toJsonString(Arrays.asList(runTaskVO));
    }

    @Test
    public void testToObject_list() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(runTasksJson.getBytes());
        Object runTaskVO = jsonSerializer.toObject(byteArrayInputStream, Types.listOf(RunTaskVO.class));
        assertThat(runTaskVO, instanceOf(List.class));
        assertThat(((List) runTaskVO).get(0), instanceOf(RunTaskVO.class));
    }

    @Test
    public void testToObject_object() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(runTaskJson.getBytes());
        Object runTaskVO = jsonSerializer.toObject(byteArrayInputStream, RunTaskVO.class);
        assertThat(runTaskVO, instanceOf(RunTaskVO.class));
    }

}
