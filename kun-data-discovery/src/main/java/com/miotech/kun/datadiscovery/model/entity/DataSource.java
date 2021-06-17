package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class DataSource {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long typeId;

    private String name;

    @JsonProperty("information")
    private JSONObject connectInfo;

    @JsonProperty("create_user")
    private String createUser;

    @JsonProperty("create_time")
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime createTime;

    @JsonProperty("update_user")
    private String updateUser;

    @JsonProperty("update_time")
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime updateTime;

    private List<String> tags;
}
