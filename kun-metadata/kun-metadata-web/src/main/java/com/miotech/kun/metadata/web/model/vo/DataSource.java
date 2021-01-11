package com.miotech.kun.metadata.web.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.json.simple.JSONObject;

import java.util.List;

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
    private Long createTime;

    @JsonProperty("update_user")
    private String updateUser;

    @JsonProperty("update_time")
    private Long updateTime;

    private List<String> tags;

    public DataSource() {
    }

    public DataSource(Long id, Long typeId, String name, JSONObject connectInfo, String createUser, Long createTime, String updateUser, Long updateTime, List<String> tags) {
        this.id = id;
        this.typeId = typeId;
        this.name = name;
        this.connectInfo = connectInfo;
        this.createUser = createUser;
        this.createTime = createTime;
        this.updateUser = updateUser;
        this.updateTime = updateTime;
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getConnectInfo() {
        return connectInfo;
    }

    public void setConnectInfo(JSONObject connectInfo) {
        this.connectInfo = connectInfo;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
