package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceType {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonProperty("type")
    private String name;

    List<DatasourceTypeField> fields = new ArrayList<>();

    public void addField(DatasourceTypeField field) {
        fields.add(field);
    }
}
