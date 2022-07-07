package com.miotech.kun.datadiscovery.model.entity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import com.miotech.kun.datadiscovery.model.enums.RefTableVersionStatus;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-20 22:55
 **/
@Data
public class RefTableVersionInfo {
    private Long versionId;    //版本id
    private Integer versionNumber;    //版本名称
    private Long tableId;//关联table_id
    private String versionDescription;    //版本描述
    private String tableName;
    private String databaseName;
    private String dataPath;    //数据目录
    private List<Long> glossaryList = Lists.newArrayList(); //标签集
    private List<String> ownerList = Lists.newArrayList(); //所属人
    private LinkedHashSet<RefColumn> refTableColumns = Sets.newLinkedHashSet(); //列
    private LinkedHashMap<ConstraintType, Set<String>> refTableConstraints = Maps.newLinkedHashMap();//约束
    private Boolean published; //是否发布
    private RefTableVersionStatus status; //是否发布
    private OffsetDateTime startTime;//开始时间
    private OffsetDateTime endTime; //结束时间
    private String createUser;//创建人
    private OffsetDateTime createTime;// 创建时间
    private String updateUser;//更新人
    private OffsetDateTime updateTime; //更新时间
    private boolean deleted;
    private Long datasetId;
}
