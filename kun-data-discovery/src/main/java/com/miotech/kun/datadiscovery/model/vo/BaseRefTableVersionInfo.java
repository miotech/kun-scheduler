package com.miotech.kun.datadiscovery.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.enums.RefTableVersionStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-04 18:12
 **/
@Data
public class BaseRefTableVersionInfo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long versionId;    //版本id
    private Integer versionNumber;    //版本名称
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tableId;//关联table_id
    private Boolean published; //是否发布
    private OffsetDateTime startTime;//开始时间
    private OffsetDateTime endTime; //结束时间
    private String createUser;//创建人
    private OffsetDateTime createTime;// 创建时间
    private String updateUser;//更新人
    private OffsetDateTime updateTime; //更新时间
    private boolean deleted;
    private String versionDescription;    //版本描述
    private String tableName;
    private String databaseName;
    private List<String> ownerList;
    private boolean enableEditName;
    private RefTableVersionStatus showStatus;
    private boolean edit;

    public BaseRefTableVersionInfo(RefTableVersionInfo refTableVersionInfo) {
        this.versionId = refTableVersionInfo.getVersionId();
        this.versionNumber = refTableVersionInfo.getVersionNumber();
        this.tableId = refTableVersionInfo.getTableId();
        this.published = refTableVersionInfo.getPublished();
        this.startTime = refTableVersionInfo.getStartTime();
        this.endTime = refTableVersionInfo.getEndTime();
        this.createUser = refTableVersionInfo.getCreateUser();
        this.createTime = refTableVersionInfo.getCreateTime();
        this.updateUser = refTableVersionInfo.getUpdateUser();
        this.updateTime = refTableVersionInfo.getUpdateTime();
        this.deleted = refTableVersionInfo.isDeleted();
        this.versionDescription = refTableVersionInfo.getVersionDescription();
        this.tableName = refTableVersionInfo.getTableName();
        this.databaseName = refTableVersionInfo.getDatabaseName();
        this.ownerList = refTableVersionInfo.getOwnerList();
        this.showStatus = refTableVersionInfo.getStatus();
    }

    public int showSorted() {
        switch (showStatus) {
            case UNPUBLISHED:
                return 0;
            case PUBLISHED:
                return 1;
            case HISTORY:
                return 2;
            default:
                throw new IllegalStateException(String.format("type not found:%s", showStatus));

        }
    }


}
