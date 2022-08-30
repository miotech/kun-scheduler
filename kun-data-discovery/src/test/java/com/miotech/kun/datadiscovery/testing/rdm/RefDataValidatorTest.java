package com.miotech.kun.datadiscovery.testing.rdm;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.miotech.kun.datadiscovery.constant.Constants;
import com.miotech.kun.datadiscovery.model.entity.rdm.*;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import com.miotech.kun.datadiscovery.model.vo.ValidationMessageVo;
import com.miotech.kun.datadiscovery.model.vo.ValidationResultVo;
import com.miotech.kun.datadiscovery.service.rdm.RefDataValidator;
import com.miotech.kun.datadiscovery.testing.DataDiscoveryTestBase;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-12 16:52
 **/
public class RefDataValidatorTest extends DataDiscoveryTestBase {
    @Autowired
    private RefDataValidator refDataValidator;

    @Test
    public void test_validate_column_name() {
        assertThat(RefDataValidator.validateColumnName("test1"), is(true));
    }

    @Test
    public void test_validate_table_name() {
        boolean test_name1 = RefDataValidator.validateName("000_1");
        assertThat(test_name1, is(false));
        boolean test_name2 = RefDataValidator.validateName("test-1");
        assertThat(test_name2, is(false));
        boolean test_name3 = RefDataValidator.validateName("*test");
        assertThat(test_name3, is(false));
        boolean test_name4 = RefDataValidator.validateName("test_1_test");
        assertThat(test_name4, is(true));
        boolean test_name5 = RefDataValidator.validateName("_11");
        assertThat(test_name5, is(true));
        boolean test_name6 = RefDataValidator.validateName("_11_test");
        assertThat(test_name6, is(true));
        boolean test_name7 = RefDataValidator.validateName("_11_TEST");
        assertThat(test_name7, is(true));
        boolean test_name8 = RefDataValidator.validateName("_TEST");
        assertThat(test_name8, is(true));
        boolean test_name9 = RefDataValidator.validateName("TEST_1");
        assertThat(test_name9, is(true));
    }

    @Test
    public void test_validate_column_empty_throw() {
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        refTableMetaData.setColumns(Sets.newLinkedHashSet());
        RefData refData = new RefData(Maps.newHashMap(), Lists.newArrayList());
        RefBaseTable refBaseTable = new RefBaseTable(refTableMetaData, refData);

        assertThrows(IllegalArgumentException.class, () -> refDataValidator.valid(refBaseTable));
        refTableMetaData.setColumns(null);
        assertThrows(IllegalArgumentException.class, () -> refDataValidator.valid(refBaseTable));
    }

    @Test
    public void test_validate_columns_throw() {
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        RefColumn column1 = new RefColumn("c1", 0, "error_type");
        LinkedHashSet<RefColumn> columns = Sets.newLinkedHashSet();
        columns.add(column1);
        refTableMetaData.setColumns(columns);
        RefData refData = new RefData(Maps.newHashMap(), Lists.newArrayList());
        RefBaseTable refBaseTable = new RefBaseTable(refTableMetaData, refData);

        assertThrows("column name or type is wrongful:type: error_type", IllegalArgumentException.class, () -> refDataValidator.valid(refBaseTable));
    }

    @Test
    public void test_validate_inconsistent_count() {
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        RefColumn column1 = new RefColumn("c1", 0, "string");
        LinkedHashSet<RefColumn> columns = Sets.newLinkedHashSet();
        columns.add(column1);
        refTableMetaData.setColumns(columns);
        DataRecord dataRecord = new DataRecord(new String[]{"data1", "data2"}, null, 1);
        ArrayList<DataRecord> dataRecords = Lists.newArrayList();
        dataRecords.add(dataRecord);
        RefData refData = new RefData(Maps.newHashMap(), dataRecords);
        RefBaseTable refBaseTable = new RefBaseTable(refTableMetaData, refData);

        ValidationResult valid = refDataValidator.valid(refBaseTable);

        assertThat(valid.getStatus(), is(false));
        assertThat(valid.size(), is(1));
        ValidationMessage validationMessage = valid.getValidationMessageList().get(0);
        assertThat(validationMessage.getLineNumber(), is(1L));
        assertThat(validationMessage.getMessage(), containsString("Inconsistent number of values"));
    }

    @Test
    public void test_validate_constraint() {
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        RefColumn column1 = new RefColumn("c1", 0, "string");
        RefColumn column2 = new RefColumn("c2", 1, "int");
        LinkedHashSet<RefColumn> columns = Sets.newLinkedHashSet();
        columns.add(column1);
        columns.add(column2);
        refTableMetaData.setColumns(columns);
        LinkedHashMap<ConstraintType, Set<String>> refTableConstraints = Maps.newLinkedHashMap();
        refTableConstraints.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("c1", "c2"));
        refTableMetaData.setRefTableConstraints(refTableConstraints);
        DataRecord dataRecord1 = new DataRecord(new String[]{"data1", "data2"}, null, 1);
        DataRecord dataRecord2 = new DataRecord(new String[]{"data1", "1"}, null, 2);
        DataRecord dataRecord3 = new DataRecord(new String[]{"", null}, null, 3);
        DataRecord dataRecord4 = new DataRecord(new String[]{"", null}, null, 4);
        ArrayList<DataRecord> dataRecords = Lists.newArrayList();
        dataRecords.add(dataRecord1);
        dataRecords.add(dataRecord2);
        dataRecords.add(dataRecord3);
        dataRecords.add(dataRecord4);
        RefData refData = new RefData(Maps.newHashMap(), dataRecords);
        RefBaseTable refBaseTable = new RefBaseTable(refTableMetaData, refData);

        ValidationResult valid = refDataValidator.valid(refBaseTable);
        assertThat(valid.getStatus(), is(false));
        List<ValidationMessage> validationMessageList = valid.getValidationMessageList();
        assertThat(valid.size(), is(2));
        assertThat(validationMessageList.get(0).getLineNumber(), is(1L));
        assertThat(validationMessageList.get(0).getMessage(), is("format error,column Type:int,line Numbers:1 ,data:data2,column:c2"));
        assertThat(validationMessageList.get(1).getLineNumber(), is(3L));
        assertThat(validationMessageList.get(1).getMessage(), is("constraint:PRIMARY_KEY validate is error,line Numbers:[3,4] ,data:\",null\""));

    }

    @Test
    public void test_validate_column_type_true() {
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        RefColumn column0 = new RefColumn("c0", 0, "int");
        RefColumn column1 = new RefColumn("c1", 1, "bigint");
        RefColumn column2 = new RefColumn("c2", 2, "float");
        RefColumn column3 = new RefColumn("c3", 3, "double");
        RefColumn column4 = new RefColumn("c4", 4, "decimal");
        RefColumn column5 = new RefColumn("c5", 5, "boolean");
        RefColumn column6 = new RefColumn("c6", 6, "string");
        RefColumn column7 = new RefColumn("c7", 7, "timestamp");
        RefColumn column8 = new RefColumn("c8", 8, "date");
        LinkedHashSet<RefColumn> columns = Sets.newLinkedHashSet();
        columns.add(column0);
        columns.add(column1);
        columns.add(column2);
        columns.add(column3);
        columns.add(column4);
        columns.add(column5);
        columns.add(column6);
        columns.add(column7);
        columns.add(column8);
        refTableMetaData.setColumns(columns);
        DataRecord dataRecord1 = new DataRecord(new String[]{"1", "10000000000000000", "1.000001", "1.0000000001", "10000000000000000000",
                "true", "test", "2022-07-11 07:38:58.299", "2022-07-11"}, null, 1);
        ArrayList<DataRecord> dataRecords = Lists.newArrayList();
        dataRecords.add(dataRecord1);
        RefData refData = new RefData(Maps.newHashMap(), dataRecords);
        RefBaseTable refBaseTable = new RefBaseTable(refTableMetaData, refData);

        ValidationResult valid = refDataValidator.valid(refBaseTable);

        assertThat(valid.getStatus(), is(true));
        assertThat(valid.size(), is(0));

    }

    @Test
    public void test_validate_column_type_time() {
        boolean timestamp = ColumnType.columnType("timestamp").test("2022-07-11 07:38:58.299");
        assertThat(timestamp, is(true));
        boolean date = ColumnType.columnType("date").test("2022-07-11");
        assertThat(date, is(true));
    }


    @Test
    public void test_validate_column_type_error() {
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        RefColumn column0 = new RefColumn("c0", 0, "int");
        RefColumn column1 = new RefColumn("c1", 1, "bigint");
        RefColumn column2 = new RefColumn("c2", 2, "float");
        RefColumn column3 = new RefColumn("c3", 3, "double");
        RefColumn column4 = new RefColumn("c4", 4, "decimal");
        RefColumn column5 = new RefColumn("c5", 5, "boolean");
        RefColumn column6 = new RefColumn("c6", 6, "string");
        RefColumn column7 = new RefColumn("c7", 7, "timestamp");
        RefColumn column8 = new RefColumn("c8", 8, "date");
        LinkedHashSet<RefColumn> columns = Sets.newLinkedHashSet();
        columns.add(column0);
        columns.add(column1);
        columns.add(column2);
        columns.add(column3);
        columns.add(column4);
        columns.add(column5);
        columns.add(column6);
        columns.add(column7);
        columns.add(column8);
        refTableMetaData.setColumns(columns);
        DataRecord dataRecord1 = new DataRecord(new String[]{"1.1", "test", "test",
                "true1", "0", "0", "0", "2022-07-", "686Z"}, null, 1);
        ArrayList<DataRecord> dataRecords = Lists.newArrayList();
        dataRecords.add(dataRecord1);
        RefData refData = new RefData(Maps.newHashMap(), dataRecords);
        RefBaseTable refBaseTable = new RefBaseTable(refTableMetaData, refData);

        ValidationResult valid = refDataValidator.valid(refBaseTable);

        assertThat(valid.getStatus(), is(false));
        assertThat(valid.size(), is(7));

    }

    @Test
    public void test_validate_vo() {
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        RefColumn column1 = new RefColumn("c1", 0, "string");
        RefColumn column2 = new RefColumn("c2", 1, "int");
        LinkedHashSet<RefColumn> columns = Sets.newLinkedHashSet();
        columns.add(column1);
        columns.add(column2);
        refTableMetaData.setColumns(columns);
        LinkedHashMap<ConstraintType, Set<String>> refTableConstraints = Maps.newLinkedHashMap();
        refTableConstraints.put(ConstraintType.PRIMARY_KEY, ImmutableSet.of("c1", "c2"));
        refTableMetaData.setRefTableConstraints(refTableConstraints);
        DataRecord dataRecord1 = new DataRecord(new String[]{"data1", "data2"}, null, 1);
        DataRecord dataRecord2 = new DataRecord(new String[]{"data1", "1"}, null, 2);
        DataRecord dataRecord3 = new DataRecord(new String[]{"", null}, null, 3);
        DataRecord dataRecord4 = new DataRecord(new String[]{"", null}, null, 4);
        ArrayList<DataRecord> dataRecords = Lists.newArrayList();
        dataRecords.add(dataRecord1);
        dataRecords.add(dataRecord2);
        dataRecords.add(dataRecord3);
        dataRecords.add(dataRecord4);
        RefData refData = new RefData(Maps.newHashMap(), dataRecords);
        RefBaseTable refBaseTable = new RefBaseTable(refTableMetaData, refData);

        ValidationResult valid = refDataValidator.valid(refBaseTable);
        assertThat(valid.getStatus(), is(false));
        assertThat(valid.size(), is(2));
        ValidationResultVo validationResultVo = new ValidationResultVo(valid);
        String summary = validationResultVo.getSummary();
        assertThat(summary, is(new StringJoiner(",").add(Constants.CONSTRAINT_INFO).add(Constants.DATA_FORMAT_ERROR).toString()));
        List<ValidationMessageVo> validationMessageVoList = validationResultVo.getValidationMessageVoList();
        assertThat(validationMessageVoList.size(), is(2));
        assertThat(validationMessageVoList.get(0).getLineNumber(), is(1L));
        assertThat(validationMessageVoList.get(1).getLineNumber(), is(3L));
    }


}
