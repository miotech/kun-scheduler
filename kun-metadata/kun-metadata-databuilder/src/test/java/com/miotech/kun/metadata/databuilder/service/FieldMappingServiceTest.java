package com.miotech.kun.metadata.databuilder.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.service.fieldmapping.FieldMappingService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FieldMappingServiceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Inject
    private FieldMappingService fieldMappingService;

    @Before
    public void setUp() {
        prepare(1, "^varchar.*$", "CHARACTER");
    }

    @After
    public void clearCache() {
        fieldMappingService.clear();
    }

    @Test
    public void testParse_match_one() {
        DatasetFieldType.Type type = fieldMappingService.parse(1L, "varchar(30)");
        assertThat(type.name(), is("CHARACTER"));
    }

    @Test
    public void testParse_not_match() {
        DatasetFieldType.Type unknownType = fieldMappingService.parse(1L, "decimal");
        assertThat(unknownType.name(), is("UNKNOWN"));
    }

    @Test
    public void testParse_match_more() {
        prepare(1L, "^varchar[\\d()]*$", "CHARACTER");
        DatasetFieldType.Type unknownType = fieldMappingService.parse(1L, "varchar(30");
        assertThat(unknownType.name(), is("UNKNOWN"));
    }

    private void prepare(long datasourceId, String pattern, String type) {
        operator.update(
                "INSERT INTO kun_mt_dataset_field_mapping(datasource_id, pattern, type) VALUES (?, ?, ?)",
                datasourceId,
                pattern,
                type
        );
    }

}
