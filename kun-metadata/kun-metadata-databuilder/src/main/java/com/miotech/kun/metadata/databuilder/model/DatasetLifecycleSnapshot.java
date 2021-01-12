package com.miotech.kun.metadata.databuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DatasetLifecycleSnapshot {

    private final List<Column> addColumns;
    private final List<Column> dropColumns;
    private final List<ColumnChanged> modifyColumns;

    @JsonCreator
    public DatasetLifecycleSnapshot(@JsonProperty("addColumns") List<Column> addColumns,
                                    @JsonProperty("dropColumns") List<Column> dropColumns,
                                    @JsonProperty("modifyColumns") List<ColumnChanged> modifyColumns) {
        this.addColumns = addColumns;
        this.dropColumns = dropColumns;
        this.modifyColumns = modifyColumns;
    }

    public List<Column> getAddColumns() {
        return addColumns;
    }

    public List<Column> getDropColumns() {
        return dropColumns;
    }

    public List<ColumnChanged> getModifyColumns() {
        return modifyColumns;
    }

    public static final class Column {

        private String name;
        private String type;
        private String description;
        private String rawType;

        @JsonCreator
        public Column(@JsonProperty("name") String name,
                      @JsonProperty("type") String type,
                      @JsonProperty("description") String description,
                      @JsonProperty("rawType") String rawType) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.rawType = rawType;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getRawType() {
            return rawType;
        }
    }

    public static final class ColumnChanged {

        private String name;
        private String type;
        private String rawType;
        private String beforeType;
        private String beforeRawType;

        @JsonCreator
        public ColumnChanged(@JsonProperty("name") String name,
                             @JsonProperty("type") String type,
                             @JsonProperty("rawType") String rawType,
                             @JsonProperty("beforeType") String beforeType,
                             @JsonProperty("beforeRawType") String beforeRawType) {
            this.name = name;
            this.type = type;
            this.rawType = rawType;
            this.beforeType = beforeType;
            this.beforeRawType = beforeRawType;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getRawType() {
            return rawType;
        }

        public String getBeforeType() {
            return beforeType;
        }

        public String getBeforeRawType() {
            return beforeRawType;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private List<Column> addColumns;
        private List<Column> dropColumns;
        private List<ColumnChanged> modifyColumns;

        private Builder() {
        }

        public Builder withAddColumns(List<Column> addColumns) {
            this.addColumns = addColumns;
            return this;
        }

        public Builder withDropColumns(List<Column> dropColumns) {
            this.dropColumns = dropColumns;
            return this;
        }

        public Builder withModifyColumns(List<ColumnChanged> modifyColumns) {
            this.modifyColumns = modifyColumns;
            return this;
        }

        public DatasetLifecycleSnapshot build() {
            return new DatasetLifecycleSnapshot(addColumns, dropColumns, modifyColumns);
        }
    }

}
