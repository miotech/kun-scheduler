package com.miotech.kun.metadata.databuilder.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.service.fieldmapping.FieldMappingService;
import com.miotech.kun.metadata.testing.MetadataDataBaseTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FieldMappingServiceTest extends MetadataDataBaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Inject
    private FieldMappingService fieldMappingService;

    @Before
    public void setUp() {
        prepare("HIVE", "^varchar.*$", "CHARACTER");
    }

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
        DatasetFieldType.Type unknownType = fieldMappingService.parse("HIVE", "decimal");
        assertThat(unknownType.name(), is("UNKNOWN"));
    }

    @Test
    public void testParse_match_more() {
        prepare("HIVE", "^varchar[\\d()]*$", "CHARACTER");
        DatasetFieldType.Type unknownType = fieldMappingService.parse("HIVE", "varchar(30");
        assertThat(unknownType.name(), is("UNKNOWN"));
    }

    private void prepare(String datasourceType, String pattern, String type) {
        operator.update(
                "INSERT INTO kun_mt_dataset_field_mapping(datasource_type, pattern, type) VALUES (?, ?, ?)",
                datasourceType,
                pattern,
                type
        );
    }

}
