package com.miotech.kun.datadiscovery.service.rdm.file;

import com.google.common.collect.Lists;
import com.miotech.kun.datadiscovery.model.entity.rdm.DataRecord;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefData;
import com.miotech.kun.datadiscovery.util.FormatParserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 18:19
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class RefStorageCsvFileResolver {


    public RefData resolve(LinkedHashSet<RefColumn> refTableColumns, InputStream is) throws IOException {
        if (Objects.isNull(is)) {
            throw new IOException("input is not null");
        }
        if (is.available() <= 0) {
            throw new IOException("input is not empty");
        }
        CSVFormat csvFormat = FormatParserUtils.csvFormat();
        CSVParser parse = csvFormat.parse(new InputStreamReader(is));
        Map<String, Integer> headerMap = refTableColumns.stream().collect(Collectors.toMap(RefColumn::getName, RefColumn::getIndex));
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
