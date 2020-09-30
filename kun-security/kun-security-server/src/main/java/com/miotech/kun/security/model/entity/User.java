package com.miotech.kun.security.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/6/29
 */
@Data
public class User {

    private Long id;

    @JsonProperty("username")
    private String name;
}
