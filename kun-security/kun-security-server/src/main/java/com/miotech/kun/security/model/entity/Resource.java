package com.miotech.kun.security.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2021/1/18
 */
@Data
public class Resource {

    @JsonSerialize(using= ToStringSerializer.class)
    Long id;

    String name;

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;
}
