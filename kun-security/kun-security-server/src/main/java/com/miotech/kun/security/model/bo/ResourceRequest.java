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

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;
}
