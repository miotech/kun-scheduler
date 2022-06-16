package com.miotech.kun.datadiscovery.model.bo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-16 09:55
 **/
@Data
public class GlossaryResourceRequest implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private List<Long> assetIds;
}
