package com.miotech.kun.commons.web.serializer;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.util.Types;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.commons.web.mock.MockObject;
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

        MockObject mockObject = new MockObject();
        mockObject.setId(IdGenerator.getInstance().nextId());
        mockObject.setConfig(Maps.newHashMap());

        runTaskJson = jsonSerializer.toString(mockObject);
        runTasksJson = jsonSerializer.toString(Arrays.asList(mockObject));
    }

    @Test
    public void testToObject_list() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(runTasksJson.getBytes());
        Object mockObject = jsonSerializer.toObject(byteArrayInputStream, Types.listOf(MockObject.class));
        assertThat(mockObject, instanceOf(List.class));
        assertThat(((List) mockObject).get(0), instanceOf(MockObject.class));
    }

    @Test
    public void testToObject_object() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(runTaskJson.getBytes());
        Object runTaskVO = jsonSerializer.toObject(byteArrayInputStream, MockObject.class);
        assertThat(runTaskVO, instanceOf(MockObject.class));
    }

}
