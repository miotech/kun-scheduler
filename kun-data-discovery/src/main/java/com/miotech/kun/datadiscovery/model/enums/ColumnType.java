package com.miotech.kun.datadiscovery.model.enums;


import java.util.Locale;
import java.util.function.Predicate;

public enum ColumnType {
    TINYINT("tinyint"),
    SMALLINT("smallint"),
    INT("int"),
    BIGINT("bigint"),
    FLOAT("float"),
    DOUBLE("double"),
    DECIMAL("decimal"),
    BOOLEAN("boolean"),
    STRING("string"),
    VARCHAR("varchar"),
    CHAR("char"),
    BINARY("binary"),
    TIMESTAMP("timestamp"),
    DATE("date");

    private String name;
    private Predicate<String> predicate;

    ColumnType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public ColumnType columnType(String name) {
        return ColumnType.valueOf(name.toUpperCase(Locale.ROOT));
    }


}
