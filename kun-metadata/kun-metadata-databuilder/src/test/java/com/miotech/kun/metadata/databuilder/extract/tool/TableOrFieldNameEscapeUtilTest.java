package com.miotech.kun.metadata.databuilder.extract.tool;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class TableOrFieldNameEscapeUtilTest {

    @Test
    public void testEscape() {
        String uppercaseField = "nAme";
        String name = TableOrFieldNameEscapeUtil.escape(uppercaseField);
        MatcherAssert.assertThat(name, Matchers.is("\"".concat(uppercaseField).concat("\"")));
    }

}
