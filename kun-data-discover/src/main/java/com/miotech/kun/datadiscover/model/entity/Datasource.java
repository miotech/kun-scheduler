package com.miotech.kun.datadiscover.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class Datasource {

    private Long id;

    private String type;

    private String name;

    @JsonProperty("information")
    private JSONObject connectInfo;

    @JsonProperty("create_user")
    private String createUser;

    @JsonProperty("create_time")
    private Long createTime;

    @JsonProperty("update_user")
    private String updateUser;

    @JsonProperty("update_time")
    private Long updateTime;

    private List<String> tags;
}
