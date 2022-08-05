package com.miotech.kun.datadiscovery.service.rdm.file;

import com.miotech.kun.datadiscovery.model.entity.rdm.RefBaseTable;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefData;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefTableMetaData;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 18:19
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class RefUploadCsvFileResolver extends RefUploadFileResolver {

    @Override
    public RefBaseTable resolve(InputStream is) throws IOException {
        RefData refData = getRefDataByCSV(is);
        Map<String, Integer> headerMap = refData.getHeaderMap();
        LinkedHashSet<RefColumn> columns = new LinkedHashSet<>();
        headerMap.forEach((k, v) -> columns.add(new RefColumn(k, v, ColumnType.STRING.getSimpleName())));
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        refTableMetaData.setColumns(columns);
        return new RefBaseTable(refTableMetaData, refData);
    }

}
