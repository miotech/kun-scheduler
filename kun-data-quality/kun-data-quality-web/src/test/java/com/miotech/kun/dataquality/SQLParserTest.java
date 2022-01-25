package com.miotech.kun.dataquality;

import com.alibaba.druid.util.JdbcUtils;
import com.miotech.kun.dataquality.web.model.entity.SQLParseResult;
import com.miotech.kun.dataquality.web.utils.SQLParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class SQLParserTest extends DataQualityTestBase {

    @Autowired
    private SQLParser sqlParser;

    @Test
    public void testParseQuerySQL_updateSQL() {
        String updateSQL = "update test set id = 1";
        String dbType = JdbcUtils.HIVE;
        try {
            sqlParser.parseQuerySQL(updateSQL, dbType);
        } catch (Exception e) {
            assertThat(e, instanceOf(UnsupportedOperationException.class));
        }
    }

    @Test
    public void testParseQuerySQL_withoutAlias() {
        String updateSQL = "select id from test";
        String dbType = JdbcUtils.HIVE;
        SQLParseResult expected = new SQLParseResult(ImmutableList.of("test"), ImmutableList.of("id"));

        SQLParseResult sqlParseResult = sqlParser.parseQuerySQL(updateSQL, dbType);
        assertThat(sqlParseResult, sameBeanAs(expected));
    }

    @Test
    public void testParseQuerySQL_withAlias() {
        String updateSQL = "select id as new_id from ods.test";
        String dbType = JdbcUtils.HIVE;
        SQLParseResult expected = new SQLParseResult(ImmutableList.of("ods.test"), ImmutableList.of("new_id"));

        SQLParseResult sqlParseResult = sqlParser.parseQuerySQL(updateSQL, dbType);
        assertThat(sqlParseResult, sameBeanAs(expected));
    }

}
