package com.miotech.kun.workflow.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

}
