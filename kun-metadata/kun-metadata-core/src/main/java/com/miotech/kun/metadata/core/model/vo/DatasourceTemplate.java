package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasourceTemplate {

    private final String type;

    private final Long id;

    @JsonCreator
    public DatasourceTemplate(@JsonProperty("type") String type, @JsonProperty("id") Long id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public static DatasourceTemplateBuilder newBuilder(){
        return new DatasourceTemplateBuilder();
    }


    public static final class DatasourceTemplateBuilder {
        private String type;
        private Long id;

        private DatasourceTemplateBuilder() {
        }

        public DatasourceTemplateBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public DatasourceTemplateBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public DatasourceTemplate build() {
            return new DatasourceTemplate(type, id);
        }
    }
}
