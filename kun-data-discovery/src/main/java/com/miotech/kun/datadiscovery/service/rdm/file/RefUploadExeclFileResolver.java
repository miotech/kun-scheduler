package com.miotech.kun.datadiscovery.service.rdm.file;

import com.miotech.kun.datadiscovery.model.entity.rdm.*;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.MathUtils;
import org.apache.http.ParseException;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-28 15:22
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class RefUploadExeclFileResolver extends RefUploadFileResolver {
    @Override
    public RefBaseTable resolve(InputStream is) throws IOException {
        Workbook wb = WorkbookFactory.create(is);
        //开始解析
        Sheet sheet = wb.getSheetAt(0);
        Map<String, Integer> headerMap = getHeader(sheet);
        List<DataRecord> data = getDataRecords(sheet, headerMap);
        LinkedHashSet<RefColumn> columns = new LinkedHashSet<>();
        headerMap.forEach((k, v) -> columns.add(new RefColumn(k, v, ColumnType.STRING.getSimpleName())));
        RefTableMetaData refTableMetaData = new RefTableMetaData();
        refTableMetaData.setColumns(columns);
        RefData refData = new RefData(headerMap, data);
        return new RefBaseTable(refTableMetaData, refData);
    }

    private List<DataRecord> getDataRecords(Sheet sheet, Map<String, Integer> headMap) {
        List<DataRecord> data = Lists.newArrayList();
        int firstRowIndex = sheet.getFirstRowNum() + 1;
        int lastRowIndex = sheet.getLastRowNum();
        int number = 1;
        for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历
            Row row = sheet.getRow(rIndex);
            if (rowIsEmpty(row)) {
                continue;
            }
            int firstCellIndex = row.getFirstCellNum();
            int len = headMap.size();
            String[] value = new String[len];
            for (int cIndex = firstCellIndex; cIndex < len; cIndex++) {   //遍历列
                value[cIndex] = getCellValue(row.getCell(cIndex));
            }
            DataRecord dataRecord = new DataRecord(value, headMap, number++);
            data.add(dataRecord);
        }
        return data;
    }

    private Map<String, Integer> getHeader(Sheet sheet) {
        Row header = sheet.getRow(0);
        Map<String, Integer> headMap = new LinkedHashMap<>();
        if (rowIsEmpty(header)) {
            log.error("input head is null or empty:row {}", header);
            throw new ParseException("input head is not empty");
        }
        int firstCellIndex = header.getFirstCellNum();
        int lastCellIndex = header.getLastCellNum();
        for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) {   //遍历列
            Cell cell = header.getCell(cIndex);
            String cellValue = getCellValue(cell);
            if (Objects.isNull(cellValue) || StringUtils.isBlank(cellValue)) {
                log.error("blank lines or null values in the header,row:{}", cIndex + 1);
                throw new ParseException(String.format("blank lines or null values in the header,row:%s", cIndex + 1));
            }
            if (headMap.containsKey(cellValue)) {
                log.error("Duplicate header，row name:{}", cellValue);
                throw new ParseException(String.format("Duplicate header，row name:%s", cellValue));
            }
            headMap.put(cellValue, cIndex);
        }
        return headMap;
    }

    public static String getCellValue(Cell cell) {
        String cellValue = null;
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                //数字
                cellValue = BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                break;
            case STRING:
                //字符串
                String value = String.valueOf(cell.getStringCellValue());
                if (StringUtils.isBlank(value)) {
                    break;
                }
                cellValue = value;
                break;
            case BOOLEAN:
                //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                //公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case BLANK:
                //空值
                break;
            case ERROR:
                //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }


    public static boolean rowIsEmpty(Row row) {
        if (null == row) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
