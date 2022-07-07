package com.miotech.kun.datadiscovery.model.entity.rdm;

import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Map;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 16:13
 **/
@Data
public class RefData {
    private final Map<String, Integer> headerMap;
    private final List<DataRecord> data;

}
