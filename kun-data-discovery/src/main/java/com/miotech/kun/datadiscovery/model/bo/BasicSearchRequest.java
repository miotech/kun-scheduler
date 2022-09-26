package com.miotech.kun.datadiscovery.model.bo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.common.model.PageInfo;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/6/18
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class BasicSearchRequest extends PageInfo {

    String keyword;
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime startCreateTime;
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime endCreateTime;
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime startUpdateTime;
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime endUpdateTime;

    private Map<String, Object> resourceAttributeMap;
}
