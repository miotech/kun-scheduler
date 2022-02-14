package com.miotech.kun.datadiscovery.model.bo;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Data
public class GlossaryRequest {

    private String name;

    private String description;

    private Long parentId;

    private List<Long> assetIds;

    private String createUser;

    private OffsetDateTime createTime;

    private String updateUser;

    private OffsetDateTime updateTime;
}
