package com.miotech.kun.datadiscovery.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-04 10:58
 **/
@Data
@AllArgsConstructor
public class EditRefTableVersionInfo {
    private String tableName;
    private String versionDescription;    //版本描述
    private List<Long> glossaryList; //标签集列表
    private List<String> ownerList; //所属人
}
