package com.miotech.kun.datadiscovery.model.entity;

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

    private List<GlossaryBasicInfoWithCount> children = new ArrayList<>();

    public void add(GlossaryBasicInfoWithCount child) {
        children.add(child);
    }
}
