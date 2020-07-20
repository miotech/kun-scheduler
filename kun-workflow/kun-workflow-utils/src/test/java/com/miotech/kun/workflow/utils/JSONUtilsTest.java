package com.miotech.kun.workflow.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class JSONUtilsTest {

    class ExamplePerson {
        private Integer id;
        private String name;
        private Position position;

        // POJO requires default constructor exists
        public ExamplePerson() {
        }

        public ExamplePerson(Integer id, String name, Position position) {
            this.id = id;
            this.name = name;
            this.position = position;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }
    }

    class Position {
        private String name;
        private Integer level;

        // POJO requires default constructor exists
        public Position() {
        }

        public Position(String name, Integer level) {
            this.name = name;
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }
    }

    static class ItemWithLongId {
        @JsonSerialize(using = ToStringSerializer.class)
        @JsonDeserialize(using = JsonLongFieldDeserializer.class)
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }


    @Test
    public void testPojoToJsonString() throws JsonProcessingException {
        ExamplePerson person = new ExamplePerson(
                1,
                "John",
                new Position(
                        "manager",
                        5
                )
        );
        String json = JSONUtils.toJsonString(person);
        assertEquals("{\"id\":1,\"name\":\"John\",\"position\":{\"name\":\"manager\",\"level\":5}}", json);
    }

    @Test
    public void testMapToJsonString() throws JsonProcessingException {
        Map<String, String> variable = new HashMap<>();
        variable.put("a", "A");
        variable.put("b", "B");
        String json = JSONUtils.toJsonString(variable);
        assertEquals("{\"a\":\"A\",\"b\":\"B\"}", json);
    }

    @Test
    public void testJsonStringToPojoWithLongField() {
        ItemWithLongId pojoItem = new ItemWithLongId();
        pojoItem.setId(9223372036854775801L);
        assertEquals("{\"id\":\"9223372036854775801\"}", JSONUtils.toJsonString(pojoItem));

        String json = "{\"id\": \"9223372036854775807\"}";
        ItemWithLongId deserializedItem = JSONUtils.jsonToObject(json, ItemWithLongId.class);
        assertEquals(Long.valueOf(9223372036854775807L), deserializedItem.getId());

        String json2 = "{\"id\": 123}";
        ItemWithLongId deserializedItem2 = JSONUtils.jsonToObject(json2, ItemWithLongId.class);
        assertEquals(Long.valueOf(123L), deserializedItem2.getId());

        String json3 = "{\"id\": \"1234.1234\"}";
        try {
            JSONUtils.jsonToObject(json3, ItemWithLongId.class);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(RuntimeException.class));
        }
    }
}
