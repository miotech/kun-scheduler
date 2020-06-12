package com.miotech.kun.datadiscover.model.entity;

import com.miotech.kun.datadiscover.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DatasetBasicPage extends PageInfo {

    List<DatasetBasic> datasets;
}
