package com.miotech.kun.datadiscover.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Data
public class DatasourceType {

    private Long id;

    private String name;

    List<DatasourceTypeField> fields = new ArrayList<>();

    public void addField(DatasourceTypeField field) {
        fields.add(field);
    }
}
