package com.miotech.kun.metadata.databuilder.extract.filter;

import org.junit.Assert;
import org.junit.Test;

public class HiveTableSchemaExtractFilterTest {

    @Test
    public void testFilter_pass() {
        boolean result = HiveTableSchemaExtractFilter.filter("MANAGED_TABLE");
        Assert.assertTrue(result);
    }

    @Test
    public void testFilter_intercept() {
        boolean result = HiveTableSchemaExtractFilter.filter("VIRTUAL_VIEW");
        Assert.assertFalse(result);

        result = HiveTableSchemaExtractFilter.filter("INDEX_TABLE");
        Assert.assertFalse(result);

        result = HiveTableSchemaExtractFilter.filter("EXTERNAL_TABLE");
        Assert.assertFalse(result);
    }

}
