package com.miotech.kun.workflow.core.execution;

import com.google.common.collect.Lists;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ConfigTest {
    @Test
    public void testSerializeToJSON() {
        // Prepare
        Map<String, Object> configValues = new HashMap<>();
        configValues.put("a", "a_values");
        configValues.put("b", Lists.newArrayList(1, 2, "3"));
        configValues.put("c", 12345);

        Config config = new Config(configValues);

        // Process
        String configJson = JSONUtils.toJsonString(config);

        // Validate
        assertThat(configJson, is("{\"a\":\"a_values\",\"b\":[1,2,\"3\"],\"c\":12345}"));
    }

    @Test
    public void testBuildWithNestedConfig_shouldThrowException() {
        // Prepare
        Map<String, Object> configValues = new HashMap<>();
        Map<String, Object> nested = new HashMap<>();
        configValues.put("nested", nested);

        // Process
        try {
            new Config(configValues);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testDeserialize_withValidPlainJSONObject_shouldSuccess() {
        // Prepare
        String json = "{\"a\":\"a_values\",\"b\":[1,2,\"3\"],\"c\":12345}";

        Map<String, Object> configValues = new HashMap<>();
        configValues.put("a", "a_values");
        configValues.put("b", Lists.newArrayList(1, 2, "3"));
        configValues.put("c", 12345);
        Config baselineConfig = new Config(configValues);

        // Process
        Config parsedConfig = JSONUtils.jsonToObject(json, Config.class);

        // Validate
        assertThat(parsedConfig, sameBeanAs(baselineConfig));
    }
}
