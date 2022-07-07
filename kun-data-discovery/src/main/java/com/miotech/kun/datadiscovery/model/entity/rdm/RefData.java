package com.miotech.kun.datadiscovery.model.entity.rdm;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 16:13
 **/
@Data
@AllArgsConstructor
public class RefData {
    private Map<String, Integer> headerMap;
    private final List<DataRecord> data;

}
