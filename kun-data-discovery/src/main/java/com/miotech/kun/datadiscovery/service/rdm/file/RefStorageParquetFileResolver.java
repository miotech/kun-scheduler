package com.miotech.kun.datadiscovery.service.rdm.file;

import com.google.common.collect.Lists;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.DataRecord;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.api.InitContext;
import org.apache.parquet.hadoop.api.ReadSupport;
import org.apache.parquet.io.api.RecordMaterializer;
import org.apache.parquet.schema.MessageType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 18:19
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class RefStorageParquetFileResolver {


    public RefData resolve(ParquetReader<Group> reader, LinkedHashSet<RefColumn> refTableColumns) throws IOException {
        List<RefColumn> sortColumns = refTableColumns.stream().sorted(Comparator.comparing(RefColumn::getIndex)).collect(Collectors.toList());
        Map<String, Integer> headerMap = refTableColumns.stream().collect(Collectors.toMap(RefColumn::getName, RefColumn::getIndex));
        Group simpleGroup;
        int i = 0;
        List<DataRecord> dataRecords = Lists.newArrayList();
        while ((simpleGroup = reader.read()) != null) {
            i++;
            Group finalSimpleGroup = simpleGroup;
            Object[] values = sortColumns.stream()
                    .map(refColumn -> ParquetUtils.getGroupItem(finalSimpleGroup, refColumn)).toArray();
            dataRecords.add(new DataRecord(values, headerMap, i));
        }
        return new RefData(headerMap, dataRecords);
    }

}
