package com.miotech.kun.workflow.core.execution;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ConfigDefTest {
    @Test
    public void testDefine_invalid_defaults() {
        // verify
        try {
            ConfigDef configDef = new ConfigDef();
            configDef.define("conf1", ConfigDef.Type.LONG, "hello", true, "this is conf1", "conf1");
            fail();
        } catch (Exception exception) {
            assertThat(exception, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testParse_defaults() {
        // prepare
        ConfigDef configDef = new ConfigDef();
        configDef.define("conf1", ConfigDef.Type.BOOLEAN, true, true, "this is conf1", "conf1");
        configDef.define("conf2", ConfigDef.Type.BOOLEAN, true, "this is conf2", "conf2");

        // process
        Map<String, Object> result = configDef.parse(ImmutableMap.of());

        // verify
        assertThat(result.size(), is(1));
        assertThat(result.get("conf1"), is(true));
    }

    @Test
    public void testParse_success() {
        // prepare
        ConfigDef configDef = new ConfigDef();
        configDef.define("conf1", ConfigDef.Type.BOOLEAN, true, "this is conf1", "conf1");
        configDef.define("conf2", ConfigDef.Type.LONG, true, "this is conf2", "conf2");
        configDef.define("conf3", ConfigDef.Type.STRING, true, "this is conf3", "conf3");

        // process
        Map<String, Object> result = configDef.parse(ImmutableMap.of(
                "conf1", "true",
                "conf2", "1",
                "conf3", "foo",
                "conf4", "bar"
        ));

        // verify
        assertThat(result.size(), is(3));
        assertThat(result.get("conf1"), is(true));
        assertThat(result.get("conf2"), is(1L));
        assertThat(result.get("conf3"), is("foo"));
    }

    @Test
    public void testParse_fail() {
        // prepare
        ConfigDef configDef = new ConfigDef();
        configDef.define("conf1", ConfigDef.Type.BOOLEAN, true, "this is conf1", "conf1");

        // process
        try {
            Map<String, Object> result = configDef.parse(ImmutableMap.of("conf1", "hello"));
            fail();
        } catch (Exception exception) {
            assertThat(exception, instanceOf(IllegalArgumentException.class));
        }

    }

    @Test
    public void testToJson() {
        // prepare
        ConfigDef configDef = new ConfigDef();
        configDef.define("conf1", ConfigDef.Type.BOOLEAN, true, "this is conf1", "conf1");

        // process
        String result = JSONUtils.toJsonString(configDef);

        // verify
        assertThat(result, is("[{\"name\":\"conf1\",\"type\":\"BOOLEAN\",\"reconfigurable\":true,\"documentation\":\"this is conf1\",\"displayName\":\"conf1\",\"required\":true}]"));
    }

    @Test
    public void testValidate_success() {
        // prepare
        ConfigDef configDef = new ConfigDef();
        configDef.define("conf1", ConfigDef.Type.BOOLEAN, true, "this is conf1", "conf1");
        configDef.define("conf2", ConfigDef.Type.STRING, true, "this is conf2", "conf2");
        configDef.define("conf3", ConfigDef.Type.LONG, true, "this is conf3", "conf3");
        Map<String, Object> values = ImmutableMap.of(
                "conf1", true,
                "conf2", "test",
                "conf3", 3L
        );

        // verify
        configDef.validate(values);
    }

    @Test
    public void testValidate_failed() {
        // prepare
        ConfigDef configDef = new ConfigDef();
        configDef.define("conf1", ConfigDef.Type.BOOLEAN, true, "this is conf1", "conf1");
        configDef.define("conf2", ConfigDef.Type.STRING, true, "this is conf2", "conf2");
        Map<String, Object> values = ImmutableMap.of(
                "conf1", true,
                "conf2", 1L
        );

        // verify
        try {
            configDef.validate(values);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
            assertThat(e.getMessage(), containsString("Expected type is STRING but actual is 1 for config 'conf2'"));
        }
    }
}