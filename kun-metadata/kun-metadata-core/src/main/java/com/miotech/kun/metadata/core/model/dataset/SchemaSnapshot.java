package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SchemaSnapshot {

    private final List<Schema> fields;

    @JsonCreator
    public SchemaSnapshot(@JsonProperty("fields") List<Schema> fields) {
        this.fields = fields;
    }

    public List<Schema> getFields() {
        return fields;
    }

    public static class Schema {
        private final String name;
        private final String type;
        private final String rawType;
        private final String description;
        private final boolean isPrimaryKey;
        private final boolean isNullable;

        @JsonCreator
        public Schema(@JsonProperty("name") String name,
                      @JsonProperty("type") String type,
                      @JsonProperty("rawType") String rawType,
                      @JsonProperty("description") String description,
                      @JsonProperty("isPrimaryKey") boolean isPrimaryKey,
                      @JsonProperty("isNullable") boolean isNullable) {
            this.name = name;
            this.type = type;
            this.rawType = rawType;
            this.description = description;
            this.isPrimaryKey = isPrimaryKey;
            this.isNullable = isNullable;
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

        public String getDescription() {
            return description;
        }

        public boolean getIsPrimaryKey() {
            return isPrimaryKey;
        }

        public boolean getIsNullable() {
            return isNullable;
        }

        public DatasetField convert() {
            return DatasetField.newBuilder()
                    .withName(this.name)
                    .withFieldType(new DatasetFieldType(DatasetFieldType.Type.valueOf(this.type), this.rawType))
                    .withComment(this.description)
                    .withIsPrimaryKey(this.isPrimaryKey)
                    .withIsNullable(this.isNullable)
                    .build();
        }

        public static SchemaSnapshot.Schema.Builder newBuilder() {
            return new SchemaSnapshot.Schema.Builder();
        }

        public static final class Builder {
            private String name;
            private String type;
            private String rawType;
            private String description;
            private boolean isPrimaryKey;
            private boolean isNullable;

            private Builder() {
            }

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withType(String type) {
                this.type = type;
                return this;
            }

            public Builder withRawType(String rawType) {
                this.rawType = rawType;
                return this;
            }

            public Builder withDescription(String description) {
                this.description = description;
                return this;
            }

            public Builder withIsPrimaryKey(boolean isPrimaryKey) {
                this.isPrimaryKey = isPrimaryKey;
                return this;
            }

            public Builder withIsNullable(boolean isNullable) {
                this.isNullable = isNullable;
                return this;
            }

            public Schema build() {
                return new Schema(name, type, rawType, description, isPrimaryKey, isNullable);
            }

        }

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private List<Schema> fields;

        private Builder() {
        }

        public Builder withFields(List<Schema> fields) {
            this.fields = fields;
            return this;
        }

        public SchemaSnapshot build() {
            return new SchemaSnapshot(fields);
        }
    }

}
