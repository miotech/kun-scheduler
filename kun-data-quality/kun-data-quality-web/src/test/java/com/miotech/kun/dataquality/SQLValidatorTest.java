package com.miotech.kun.dataquality;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.mock.MockDatasetBasicFactory;
import com.miotech.kun.dataquality.mock.MockValidateMetricsRequestFactory;
import com.miotech.kun.dataquality.web.model.bo.ValidateMetricsRequest;
import com.miotech.kun.dataquality.web.model.entity.DatasetBasic;
import com.miotech.kun.dataquality.web.model.entity.SQLParseResult;
import com.miotech.kun.dataquality.web.model.entity.ValidateMetricsResult;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import com.miotech.kun.dataquality.web.utils.SQLValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;

public class SQLValidatorTest extends DataQualityTestBase {

    @Autowired
    private SQLValidator sqlValidator;

    @SpyBean
    private DatasetRepository datasetRepository;

    @Test
    public void testValidate_databaseNameNotMatch() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String type = "AWS";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, type);

        Long datasetId = IdGenerator.getInstance().nextId();
        String sqlText = String.format("select %s from %s.%s", field, database, table);
        ValidateMetricsRequest vsr = MockValidateMetricsRequestFactory.create(datasetId, sqlText, field);
        doReturn(datasetBasic).when(datasetRepository).findBasic(vsr.getMetrics().getDatasetGid());

        String otherDatabase = "other_db";
        SQLParseResult sqlParseResult = new SQLParseResult(ImmutableList.of(String.format("%s.%s", otherDatabase, table)), ImmutableList.of(field));
        ValidateMetricsResult result = sqlValidator.validate(sqlParseResult, vsr);
        assertThat(result.isSuccess(), is(false));
        assertThat(result.getMessage(), is("Not related to current dataset."));
    }

    @Test
    public void testValidate_tableNameNotMatch() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String type = "AWS";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, type);

        Long datasetId = IdGenerator.getInstance().nextId();
        String sqlText = String.format("select %s from %s.%s", field, database, table);
        ValidateMetricsRequest vsr = MockValidateMetricsRequestFactory.create(datasetId, sqlText, field);
        doReturn(datasetBasic).when(datasetRepository).findBasic(vsr.getMetrics().getDatasetGid());

        String otherTable = "other_table";
        SQLParseResult sqlParseResult = new SQLParseResult(ImmutableList.of(String.format("%s.%s", database, otherTable)), ImmutableList.of(field));
        ValidateMetricsResult result = sqlValidator.validate(sqlParseResult, vsr);
        assertThat(result.isSuccess(), is(false));
        assertThat(result.getMessage(), is("Not related to current dataset."));
    }

    @Test
    public void testValidate_columnNameNotMatch() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String type = "AWS";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, type);

        Long datasetId = IdGenerator.getInstance().nextId();
        String sqlText = String.format("select %s from %s.%s", field, database, table);
        ValidateMetricsRequest vsr = MockValidateMetricsRequestFactory.create(datasetId, sqlText, field);
        doReturn(datasetBasic).when(datasetRepository).findBasic(vsr.getMetrics().getDatasetGid());

        String otherField = "other_field";
        SQLParseResult sqlParseResult = new SQLParseResult(ImmutableList.of(String.format("%s.%s", database, table)), ImmutableList.of(otherField));
        ValidateMetricsResult result = sqlValidator.validate(sqlParseResult, vsr);
        assertThat(result.isSuccess(), is(false));
        assertThat(result.getMessage(), is("The column names returned in the SQL statement are inconsistent with the validation rules."));
    }

    @Test
    public void testValidate_match() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String type = "AWS";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, type);

        Long datasetId = IdGenerator.getInstance().nextId();
        String sqlText = String.format("select %s from %s.%s", field, database, table);
        ValidateMetricsRequest vsr = MockValidateMetricsRequestFactory.create(datasetId, sqlText, field);
        doReturn(datasetBasic).when(datasetRepository).findBasic(vsr.getMetrics().getDatasetGid());

        SQLParseResult sqlParseResult = new SQLParseResult(ImmutableList.of(String.format("%s.%s", database, table)), ImmutableList.of(field));
        ValidateMetricsResult result = sqlValidator.validate(sqlParseResult, vsr);
        assertThat(result.isSuccess(), is(true));
    }

    @Test
    public void testValidateColumnNames_paramIsBlank() {
        List<String> expected = ImmutableList.of("id");
        List<String> received = null;
        boolean result = sqlValidator.validateColumnNames(expected, received);
        assertThat(result, is(false));

        received = ImmutableList.of();
        result = sqlValidator.validateColumnNames(expected, received);
        assertThat(result, is(false));
    }

    @Test
    public void testValidateColumnNames_mismatch() {
        List<String> expected = ImmutableList.of("id");
        List<String> received = ImmutableList.of("foo", "bar");
        boolean result = sqlValidator.validateColumnNames(expected, received);
        assertThat(result, is(false));

        received = ImmutableList.of("foo");
        result = sqlValidator.validateColumnNames(expected, received);
        assertThat(result, is(false));
    }

    @Test
    public void testValidateColumnNames_match() {
        List<String> expected = ImmutableList.of("foo", "bar");
        List<String> received = ImmutableList.of("foo", "bar");
        boolean result = sqlValidator.validateColumnNames(expected, received);
        assertThat(result, is(true));

        received = ImmutableList.of("bar", "foo");
        result = sqlValidator.validateColumnNames(expected, received);
        assertThat(result, is(true));
    }

}
