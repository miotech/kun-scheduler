package com.miotech.kun.datadiscovery.testing.rdm;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.datadiscovery.model.entity.rdm.DataRecord;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-08 16:13
 **/
public class DataRecordTest {

    @Test
    public void test_data_record() {
        String[] values = {"test", "10", "data_test"};
        Map<String, Integer> mapping = ImmutableMap.of("name", 0, "age", 1, "data", 2);
        DataRecord dataRecord = new DataRecord(values, mapping, 1);
        assertThat(dataRecord.get(0), is(values[0]));
        assertThat(dataRecord.get(1), is(values[1]));
        assertThat(dataRecord.get(2), is(values[2]));
        assertThat(dataRecord.get("name"), is(values[0]));
        assertThat(dataRecord.get("age"), is(values[1]));
        assertThat(dataRecord.get("data"), is(values[2]));
        assertThat(dataRecord.getRecordNumber(), is(1L));
        assertThat(dataRecord.isConsistent(), is(true));
        assertThat(dataRecord.isMapped("name"), is(true));
        assertThat(dataRecord.isSet("name"), is(true));
        assertThat(dataRecord.size(), is(values.length));
        assertThat(dataRecord.size(), is(values.length));
        assertThat(dataRecord.toString(), is("CSVRecord [ mapping=" + mapping +
                ", recordNumber=" + 1 + ", values=" +
                Arrays.toString(values) + "]"));
    }
}
