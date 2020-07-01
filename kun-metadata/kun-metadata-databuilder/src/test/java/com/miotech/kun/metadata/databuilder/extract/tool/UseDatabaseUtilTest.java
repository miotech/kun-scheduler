package com.miotech.kun.metadata.databuilder.extract.tool;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class UseDatabaseUtilTest {

    private static final String URL_END_WITH_SLASH = "localhost:port/";
    private static final String URL = "localhost:port";
    private static final String DATABASE = "test_database";
    private static final String SCHEMA = "test_schema";

    @Test
    public void testUseDatabase_endWithSlash() {
        String s = UseDatabaseUtil.useDatabase(URL_END_WITH_SLASH, DATABASE);
        MatcherAssert.assertThat(s, Matchers.is(URL_END_WITH_SLASH.concat(DATABASE)));
    }

    @Test
    public void testUseDatabase() {
        MatcherAssert.assertThat(UseDatabaseUtil.useDatabase(URL, DATABASE), Matchers.is(URL.concat("/").concat(DATABASE)));
    }

    @Test
    public void testUseSchema_endWithSlash() {
        MatcherAssert.assertThat(UseDatabaseUtil.useSchema(URL_END_WITH_SLASH, DATABASE, SCHEMA),
                Matchers.is(URL_END_WITH_SLASH.concat(DATABASE).concat("?currentSchema=").concat(SCHEMA)));
    }

    @Test
    public void testUseSchema() {
        MatcherAssert.assertThat(UseDatabaseUtil.useSchema(URL, DATABASE, SCHEMA),
                Matchers.is(URL.concat("/").concat(DATABASE).concat("?currentSchema=").concat(SCHEMA)));
    }

}
