package com.miotech.kun.metadata.databuilder.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.service.fieldmapping.FieldMappingService;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FieldMappingServiceTest extends DatabaseTestBase {

    @Inject
    private FieldMappingService fieldMappingService;

    @After
    public void clearCache() {
        fieldMappingService.clear();
    }

    @Test
    public void testParse_match_one() {
        DatasetFieldType.Type type = fieldMappingService.parse("HIVE", "varchar(30)");
        assertThat(type.name(), is("CHARACTER"));
    }

    @Test
    public void testParse_not_match() {
        DatasetFieldType.Type unknownType = fieldMappingService.parse("HIVE", "not_exist_type");
        assertThat(unknownType.name(), is("UNKNOWN"));
    }

}
