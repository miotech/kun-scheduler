package com.miotech.kun.common.helpers;

import com.miotech.kun.workflow.core.model.common.Param;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OperatorDaoHelpersTest {
    @Test
    public void testJsonStringToParams() {
        String json = "[" +
                "{ \"name\": \"foo\", \"description\": \"foobar\", \"value\": \"bar\" }," +
                "{ \"name\": \"foo2\", \"description\": \"foobar2\", \"value\": \"bar2\" }" +
                "]";
        List<Param> params = OperatorDaoHelpers.jsonStringToParams(json);
        assertThat(params.size(), is(2));
        assertThat(params.get(1).getName(), is("foo2"));
    }

    @Test
    public void testParamsToJsonString() {
        List<Param> emptyParams = new ArrayList<>();
        String emptyJson = OperatorDaoHelpers.paramsToJsonString(emptyParams);
        assertThat(emptyJson, is("[]"));

        List<Param> params = new ArrayList<>();

        Param param0 = Param.newBuilder().withName("foo").withDescription("foobar").withValue("bar").build();
        Param param1 = Param.newBuilder().withName("foo2").withDescription("foobar2").withValue("bar2").build();
        params.add(param0);
        params.add(param1);

        String json = OperatorDaoHelpers.paramsToJsonString(params);
        String expectJson = "[{\"name\":\"foo\",\"description\":\"foobar\",\"value\":\"bar\"},{\"name\":\"foo2\",\"description\":\"foobar2\",\"value\":\"bar2\"}]";
        assertThat(json, is(expectJson));
    }
}
