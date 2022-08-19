package com.miotech.kun.datadiscovery.service.rdm.file;

import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import org.apache.commons.lang3.StringUtils;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;
import org.apache.parquet.schema.Types;
import org.apache.spark.sql.execution.datasources.parquet.ParquetToSparkSchemaConverter;
import org.apache.spark.sql.execution.datasources.parquet.SparkToParquetSchemaConverter;
import org.apache.spark.sql.internal.SQLConf;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @program: demo
 * @description:
 * @author: zemin  huang
 * @create: 2022-08-11 13:40
 **/


public class ParquetUtils {
    private static final SparkToParquetSchemaConverter SPARK_TO_PARQUET_SCHEMA_CONVERTER;
    private static final ParquetToSparkSchemaConverter PARQUET_TO_SPARK_SCHEMA_CONVERTER;

    static {
        SPARK_TO_PARQUET_SCHEMA_CONVERTER = new SparkToParquetSchemaConverter(false, SQLConf.get().parquetOutputTimestampType());
        PARQUET_TO_SPARK_SCHEMA_CONVERTER = new ParquetToSparkSchemaConverter(true, true);
    }

    public static Object getGroupItem(Group group, RefColumn refColumn) {
        if (group.getFieldRepetitionCount(refColumn.getName()) <= 0) {
            return null;
        }
        ColumnType columnType = ColumnType.columnType(refColumn.getColumnType());
        switch (columnType) {
            case TIMESTAMP:
                return ParquetConverter.getTimestampString(group.getBinary(refColumn.getName(), 0));
            case DATE:
                return ParquetConverter.getDateString(group.getInteger(refColumn.getName(), 0));
            case DECIMAL:

                return ParquetConverter.getBigDecimalString(group.getBinary(refColumn.getName(), 0));
            case DOUBLE:
            case STRING:
            case BOOLEAN:
            case FLOAT:
            case BIGINT:
            case INT:
            default:
                return group.getValueToString(refColumn.getIndex(), 0);
        }
    }

    public static void addGroupItem(ColumnType columnType, Group group, String key, Object value) {
        if (Objects.isNull(value)) {
            return;
        }
        String valueString = String.valueOf(value);
        if (StringUtils.isBlank(valueString)) {
            return;
        }
        switch (columnType) {
            case TIMESTAMP:
                group.add(key, Binary.fromConstantByteArray(ParquetConverter.convertTimeStamp(Timestamp.valueOf(valueString))));
                break;
            case DATE:
                group.add(key, ParquetConverter.convertDate(valueString));
                break;
            case DECIMAL:
                group.add(key, ParquetConverter.convertBigDecimal(valueString));
                break;
            case STRING:
                group.add(key, Binary.fromString(valueString));
                break;
            case BOOLEAN:
                group.add(key, Boolean.parseBoolean(valueString));
                break;
            case DOUBLE:
                group.add(key, Double.parseDouble(valueString));
                break;
            case FLOAT:
                group.add(key, Float.parseFloat(valueString));
                break;
            case BIGINT:
                group.add(key, Long.parseLong(valueString));
                break;
            case INT:
                group.add(key, Integer.parseInt(valueString));
                break;
            default:
                throw new IllegalArgumentException(String.format("%s type is not support",columnType));
        }

    }


    public static String sparkSchemaJson(MessageType messageType) {
        StructType convert = PARQUET_TO_SPARK_SCHEMA_CONVERTER.convert(messageType);
        return convert.json();
    }


    public static MessageType createMessageType(String schema, LinkedHashSet<RefColumn> columns) {
        List<RefColumn> collect = columns.stream().sorted(Comparator.comparing(RefColumn::getIndex)).collect(Collectors.toList());
        Types.MessageTypeBuilder messageTypeBuilder = Types.buildMessage();
        collect.forEach(refColumn -> messageTypeBuilder.addField(getPrimitiveType(refColumn.getName(), ColumnType.dataType(refColumn.getColumnType()))).named(refColumn.getName()));
        return messageTypeBuilder.named(schema);
    }

    private static Type getPrimitiveType(String filedName, DataType dataType) {
        return SPARK_TO_PARQUET_SCHEMA_CONVERTER.convertField(new StructField(filedName, dataType, true, new Metadata()));
    }


}