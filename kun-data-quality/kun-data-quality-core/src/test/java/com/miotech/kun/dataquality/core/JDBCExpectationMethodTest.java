package com.miotech.kun.dataquality.core;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.dataquality.core.expectation.JDBCExpectationMethod;
import org.junit.jupiter.api.Test;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JDBCExpectationMethodTest {


    @Test
    public void testRemoveEndSemicolon_includeSemicolon() {
        String sql = "select 1 = 1;";
        JDBCExpectationMethod method = new JDBCExpectationMethod(sql, ImmutableList.of());

        assertThat(method.removeEndSemicolon(), is("select 1 = 1"));
    }

    @Test
    public void testRemoveEndSemicolon_includeSemicolonAndSpace() {
        String sql = "select 1 = 1; ";
        JDBCExpectationMethod method = new JDBCExpectationMethod(sql, ImmutableList.of());

        assertThat(method.removeEndSemicolon(), is("select 1 = 1"));
    }

}
