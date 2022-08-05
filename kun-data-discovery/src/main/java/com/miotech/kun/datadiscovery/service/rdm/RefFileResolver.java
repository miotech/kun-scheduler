package com.miotech.kun.datadiscovery.service.rdm;

import com.google.common.collect.Lists;
import com.miotech.kun.datadiscovery.model.entity.rdm.DataRecord;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefData;
import com.miotech.kun.datadiscovery.util.FormatParserUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 18:13
 **/
public interface RefFileResolver<T> {

    T resolve(InputStream is) throws IOException;

    default RefData getRefDataByCSV(InputStream is) throws IOException {
        if (Objects.isNull(is)) {
            throw new IOException("input is not null");
        }
        if (is.available() <= 0) {
            throw new IOException("input is not empty");
        }
        CSVFormat csvFormat = FormatParserUtils.withHeaderCSVFormat();
        CSVParser parse = csvFormat.parse(new InputStreamReader(is));
        Map<String, Integer> headerMap = parse.getHeaderMap();
        List<CSVRecord> records = parse.getRecords();
        List<DataRecord> dataRecords = Lists.newArrayList();
        for (CSVRecord record : records) {
            Spliterator<String> spliterator = record.spliterator();
            Object[] values = StreamSupport.stream(spliterator, false).toArray();
            dataRecords.add(new DataRecord(values, headerMap, record.getRecordNumber()));
        }
        return new RefData(headerMap, dataRecords);
    }
}
