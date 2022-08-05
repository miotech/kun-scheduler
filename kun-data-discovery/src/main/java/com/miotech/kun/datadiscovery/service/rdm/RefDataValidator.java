package com.miotech.kun.datadiscovery.service.rdm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.miotech.kun.datadiscovery.constant.Constants;
import com.miotech.kun.datadiscovery.model.entity.rdm.*;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.miotech.kun.datadiscovery.constant.Constants.INPUT_MAX_LINE;

@Component
@Slf4j
public class RefDataValidator {
    public ValidationResult valid(RefBaseTable refBaseTable) {
        Preconditions.checkNotNull(refBaseTable, "refBaseTable does not null");
        RefTableMetaData refTableMetaData = refBaseTable.getRefTableMetaData();
        LinkedHashSet<RefColumn> columns = refTableMetaData.getColumns();
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(columns), "columns is not empty");
        String validateTblColumnString = validateTblColumns(columns);
        if (StringUtils.isNotBlank(validateTblColumnString)) {
            log.error(String.format("column name or type is wrongful:%s", validateTblColumnString));
            throw new IllegalArgumentException(String.format("column name or type is wrongful:%s", validateTblColumnString));
        }

        RefData refData = refBaseTable.getRefData();
        List<DataRecord> data = refData.getData();
        if (data.size() < 1) {
            log.error("The number of rows cannot be less than 1,size:{}", data.size());
            throw new IllegalArgumentException(String.format("The number of rows cannot be less than 1:%s", data.size()));
        }
        if (data.size() > INPUT_MAX_LINE) {
            log.error("The number of rows cannot be greater than {} ,size:{}", INPUT_MAX_LINE, data.size());
            throw new IllegalArgumentException(String.format("he number of rows cannot be greater than %s ,size:%s", INPUT_MAX_LINE, data.size()));
        }
        Map<String, Integer> headerMap = refData.getHeaderMap();
        if (Objects.isNull(headerMap) || headerMap.isEmpty()) {
            headerMap = new LinkedHashMap<>();
            for (RefColumn column : columns) {
                headerMap.put(column.getName(), column.getIndex());
            }
            refData.setHeaderMap(headerMap);
        }
        log.debug("headerMap:{}", headerMap);
        ValidationResult validationResult = new ValidationResult();

        if (!validInconsistentCount(validationResult, headerMap, data)) {
            validationResult.addSummary(Constants.INCONSISTENT_INFO);
            return validationResult;
        }
        LinkedHashMap<ConstraintType, Set<String>> constraintMap = refTableMetaData.getRefTableConstraints();
        log.debug("constraintMap:{}", constraintMap);
        if ((Objects.nonNull(constraintMap)) && (!constraintMap.isEmpty()) && (!validConstraint(validationResult, constraintMap, headerMap, data))) {
            validationResult.addSummary(Constants.CONSTRAINT_INFO);
        }
        if (!validColumnsData(validationResult, columns, data)) {
            validationResult.addSummary(Constants.DATA_FORMAT_ERROR);
        }
        return validationResult;
    }

    private ArrayListMultimap<String, String> columnToRow(LinkedHashSet<RefColumn> columns, List<DataRecord> dataRecordList) {
        ArrayListMultimap<String, String> dataMap = ArrayListMultimap.create();
        for (DataRecord dataRecord : dataRecordList) {
            columns.forEach(column -> {
                Object value = dataRecord.get(column.getName());
                if (Objects.isNull(value)) {
                    dataMap.put(column.getName(), null);
                } else {
                    dataMap.put(column.getName(), String.valueOf(value));

                }
            });
        }
        return dataMap;
    }

    private boolean validColumnsData(ValidationResult result, LinkedHashSet<RefColumn> columns, List<DataRecord> dataRecordList) {
        ArrayListMultimap<String, String> dataMap = columnToRow(columns, dataRecordList);
        List<ValidationMessage> validationMessageList = Flux.fromIterable(columns)
                .flatMap(refColumn -> Mono.fromCallable(() -> validDataType(refColumn, dataMap.get(refColumn.getName()))).subscribeOn(Schedulers.boundedElastic()), columns.size())
                .toStream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        result.addValidationMessage(validationMessageList);
        return result.getStatus();
    }

    private ArrayList<ValidationMessage> validDataType(RefColumn refColumn, List<String> valueList) {
        ArrayList<ValidationMessage> validationMessages = Lists.newArrayList();
        String columnTypeString = refColumn.getColumnType();
        for (int i = 0; i < valueList.size(); i++) {
            String value = valueList.get(i);
            ColumnType columnType = ColumnType.columnType(columnTypeString);
            if (!columnType.test(value)) {
                validationMessages.add(ValidationMessage.dataFormatErrorMessage(refColumn.getName(), refColumn.getColumnType(), (i + 1L), value));
            }
        }
        return validationMessages;
    }

    private boolean validConstraint(ValidationResult result, LinkedHashMap<ConstraintType, Set<String>> constraintMap,
                                    Map<String, Integer> headerMap, List<DataRecord> dataRecordList) {
        Set<String> columns = constraintMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        if (!columns.stream().allMatch(headerMap::containsKey)) {
            log.error("The column to be checked does not exist:{}", columns);
            throw new IllegalArgumentException(String.format("The column to be checked does not exist:%s", columns));
        }
        for (Map.Entry<ConstraintType, Set<String>> entry : constraintMap.entrySet()) {
            if (!CollectionUtils.isEmpty(entry.getValue())) {
                ConstraintType constraintType = entry.getKey();
                List<ValidationMessage> validationMessageList = validConstraintColumn(constraintType, entry.getValue(), dataRecordList);
                result.addValidationMessage(validationMessageList);
            }
        }
        return result.getStatus();
    }

    private List<ValidationMessage> validConstraintColumn(ConstraintType constraintType, Set<String> columns, List<DataRecord> dataRecordList) {
        switch (constraintType) {
            case PRIMARY_KEY:
                return validPrimaryKey(constraintType, columns, dataRecordList);
            default:
                log.error("The constraint type to be checked does not exist:{}", constraintType);
                throw new IllegalArgumentException(String.format("The constraint type to be checked does not exist:%s", constraintType));
        }

    }

    private boolean validInconsistentCount(ValidationResult result, Map<String, Integer> headerMap, List<DataRecord> dataRecordList) {
        for (DataRecord dataRecord : dataRecordList) {
            if (!dataRecord.isMapping()) {
                dataRecord.setMapping(headerMap);
            }
            if (!dataRecord.isConsistent()) {
                result.addValidationMessage(ValidationMessage.inconsistentCountMessage(dataRecord.getRecordNumber(), dataRecord.getValues(), headerMap));
            }
        }
        return result.getStatus();
    }

    private List<ValidationMessage> validPrimaryKey(ConstraintType constraintType, Set<String> columns, List<DataRecord> dataRecordList) {
        ArrayList<ValidationMessage> validationMessages = Lists.newArrayList();
        ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
        for (int i = 0; i < dataRecordList.size(); i++) {
            DataRecord dataRecord = dataRecordList.get(i);
            boolean nullValue = columns.stream().map(dataRecord::get).collect(Collectors.toList()).stream().anyMatch(Objects::isNull);
            String value = columns.stream().map(column -> String.valueOf(dataRecord.get(column))).collect(Collectors.joining(","));
            if (nullValue) {
                validationMessages.add(ValidationMessage.constraintErrorMessage(constraintType, value, ImmutableSet.of((i + 1))));
            }
            multimap.put(value, i + 1);
        }
        multimap.asMap().forEach((data, lines) -> {
            if (CollectionUtils.isNotEmpty(lines) && Objects.nonNull(data) && lines.size() >= 2) {
                validationMessages.add(ValidationMessage.constraintErrorMessage(constraintType, data, lines));
            }
        });
        return validationMessages;
    }

    public static boolean validateName(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        Pattern tpat = Pattern.compile("(^[_a-zA-Z])+([_a-zA-Z0-9])*");
        Matcher m = tpat.matcher(name);
        return m.matches();
    }

    public static String validateTblColumns(Set<RefColumn> cols) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (RefColumn refColumn : cols) {
            if (!validateColumnName(refColumn.getName())) {
                stringJoiner.add("name: " + refColumn.getName());
            }
            if (!validateColumnType(refColumn.getColumnType())) {
                stringJoiner.add("type: " + refColumn.getColumnType());
            }
        }
        return stringJoiner.toString();
    }

    public static boolean validateColumnType(String type) {
        return ColumnType.existColumnType(type);

    }

    /*
     * At the Metadata level there are no restrictions on Column Names.
     */
    public static boolean validateColumnName(String name) {
        return StringUtils.isNotBlank(name) && validateName(name);
    }
}
