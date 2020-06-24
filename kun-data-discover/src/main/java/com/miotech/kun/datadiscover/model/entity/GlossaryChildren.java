package com.miotech.kun.datadiscover.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/18
 */
@Data
public class GlossaryChildren {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long parentId;

    private List<GlossaryBasic> children = new ArrayList<>();

    public void add(GlossaryBasic child) {
        children.add(child);
    }
}
