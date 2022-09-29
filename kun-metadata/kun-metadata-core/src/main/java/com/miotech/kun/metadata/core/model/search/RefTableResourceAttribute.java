package com.miotech.kun.metadata.core.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-25 19:39
 **/
public class RefTableResourceAttribute extends ResourceAttribute {
    private List<Map<Long, String>> glossaries;

    @JsonCreator
    public RefTableResourceAttribute(
            @JsonProperty("glossaries") List<Map<Long, String>> glossaries,
            @JsonProperty("owners") String owners) {
        super(owners);
        this.glossaries = glossaries;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public List<Map<Long, String>> getGlossaries() {
        return glossaries;
    }

    public static final class Builder {
        private List<Map<Long, String>> glossaries;
        private String owners;

        private Builder() {
        }

        public static Builder aRefTableResourceAttribute() {
            return new Builder();
        }

        public Builder withGlossaries(List<Map<Long, String>> glossaries) {
            this.glossaries = glossaries;
            return this;
        }

        public Builder withOwners(String owners) {
            this.owners = owners;
            return this;
        }

        public RefTableResourceAttribute build() {
            return new RefTableResourceAttribute(glossaries, owners);
        }
    }

    @Override
    public String toString() {
        return "RefTableResourceAttribute{" +
                "glossaries=" + glossaries +
                "} " + super.toString();
    }
}
