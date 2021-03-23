package com.miotech.kun.security.model.bo;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2021/1/17
 */
@Data
public class ResourceRequest {

    String resourceName;

    Long belongToResourceId;

    Boolean isRoot;

    Long createUser;

    Long createTime;

    Long updateUser;

    Long updateTime;
}
