package com.miotech.kun.datadiscovery.model.entity.rdm;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 16:11
 **/
@Data
@AllArgsConstructor
public class RefColumn {
    private final String name;
    private final Integer index;
    private final String columnType;
}
