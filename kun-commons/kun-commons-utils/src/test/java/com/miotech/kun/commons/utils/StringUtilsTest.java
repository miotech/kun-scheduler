package com.miotech.kun.commons.utils;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void resolveWithVariable() {
        String text = "{{a}}";
        String result = StringUtils.resolveWithVariable(text, ImmutableMap.of("a", "b"));
        assertThat(result, is("b"));
    }

    @Test
    public void resolveWithVariable_with_multiple() {
        String text = "{{a}}{{c}}";
        String result = StringUtils.resolveWithVariable(text, ImmutableMap.of("a", "b", "c", "d"));
        assertThat(result, is("bd"));
    }

    @Test
    public void resolveWithVariable_with_error() {
        String text = "{{a}}";
        try {
            StringUtils.resolveWithVariable(text, ImmutableMap.of());
        } catch (Exception e) {
            assertThat(e.getClass(), is(IllegalArgumentException.class));
            assertThat(e.getMessage(), is("Cannot resolve variable key `a`"));
        }
    }
}