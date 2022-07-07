package com.miotech.kun.datadiscovery.service.rdm.file;

import com.miotech.kun.datadiscovery.model.entity.rdm.*;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.http.ParseException;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
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
        headerMap.forEach((k, v) -> columns.add(new RefColumn(k, v, ColumnType.STRING.name())));
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
            int lastCellIndex = row.getLastCellNum();
            String[] value = new String[lastCellIndex - firstCellIndex];
            for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) {   //遍历列
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
            headMap.put(getCellValue(cell), cIndex);
        }
        return headMap;
    }

    public static String getCellValue(Cell cell) {
        String cellValue = "";
        if (cell == null) {
            return cellValue;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                //数字
                cellValue = stringDateProcess(cell);
                break;
            case STRING:
                //字符串
                cellValue = String.valueOf(cell.getStringCellValue());
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
                cellValue = "";
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

    public static String stringDateProcess(Cell cell) {
        String result;
        if (DateUtil.isCellDateFormatted(cell)) {
            // 处理日期格式、时间格式
            SimpleDateFormat sdf = null;
            if (cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("h:mm")) {
                sdf = new SimpleDateFormat("HH:mm");
            } else {// 日期
                sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            }
            Date date = cell.getDateCellValue();
            result = sdf.format(date);
        } else if (cell.getCellStyle().getDataFormat() == 58) {
            // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            double value = cell.getNumericCellValue();
            Date date = DateUtil
                    .getJavaDate(value);
            result = sdf.format(date);
        } else {
            double value = cell.getNumericCellValue();
            CellStyle style = cell.getCellStyle();
            DecimalFormat format = new DecimalFormat();
            String temp = style.getDataFormatString();
            // 单元格设置成常规
            if (temp.equals("General")) {
                format.applyPattern("0");
            }
            result = format.format(value);
        }

        return result;
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
