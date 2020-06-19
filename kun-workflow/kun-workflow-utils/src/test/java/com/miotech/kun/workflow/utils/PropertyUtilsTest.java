package com.miotech.kun.workflow.utils;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PropertyUtilsTest {

    @Test
    public void replaceValueFromEnvironment_single() {
        String rawText =  "a${TEST_ENV}c";
        Map<String, String> envMap = new HashMap<>();
        envMap.put("TEST_ENV", "b");

        setEnv(envMap);
        String result = PropertyUtils.replaceValueFromEnvironment(rawText);
        assertThat(result, Matchers.is("abc"));
    }

    @Test
    public void replaceValueFromEnvironment_multiple() {
        String rawText =  "a${TEST_ENV1}c${TEST_ENV1}d${TEST_ENV2}";
        Map<String, String> envMap = new HashMap<>();
        envMap.put("TEST_ENV1", "b");
        envMap.put("TEST_ENV2", "e");

        setEnv(envMap);
        String result = PropertyUtils.replaceValueFromEnvironment(rawText);
        assertThat(result, Matchers.is("abcbde"));
    }

    protected static void setEnv(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field;
                    try {
                        field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    } catch (NoSuchFieldException | IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }
}