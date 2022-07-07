package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;


/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:34
 **/
@Data
public class ValidationMessage {
    @JsonSerialize(using = ToStringSerializer.class)
    private final Long lineNumber;
    private final String columnName;
    private final ValidationType validationType;
    private final String message;


    private ValidationMessage(@NotNull Long lineNumber, String columnName, ValidationType validationType, String message) {
        if (Objects.isNull(lineNumber)) {
            throw new IllegalArgumentException("lineNumber is not null");
        }
        this.lineNumber = lineNumber;
        this.columnName = columnName;
        this.validationType = validationType;
        this.message = message;
    }

    public static ValidationMessage inconsistentCountMessage(Long lineNumber, Object[] data, Map<String, Integer> headerMap) {
        return new ValidationMessage(lineNumber, null, ValidationType.INCONSISTENT,
                String.format("Inconsistent number of values，lineNumber:%s,header:%s,data:%s", lineNumber, JSONUtils.toJsonString(headerMap), JSONUtils.toJsonString(data)));
    }


    public static ValidationMessage constraintErrorMessage(ConstraintType constraintType, String data, Collection<@Nullable Integer> lineNumbers) {
        long lineNumber = Objects.requireNonNull(lineNumbers.iterator().next()).longValue();
        return new ValidationMessage(lineNumber, null, ValidationType.CONSTRAINT_PRIMARY_KEY,
                String.format("constraint:%s validate is error,line Numbers:%s ,data:%s", constraintType, JSONUtils.toJsonString(lineNumbers), JSONUtils.toJsonString(data)));
    }

    public static ValidationMessage dataFormatErrorMessage(String column, String columnType, Long lineNumber, String value) {
        return new ValidationMessage(lineNumber, column, ValidationType.DATA_FORMAT_ERROR,
                String.format("format error,column Type:%s,line Numbers:%s ,data:%s,column:%s", columnType, lineNumber, value, column));
    }
}
