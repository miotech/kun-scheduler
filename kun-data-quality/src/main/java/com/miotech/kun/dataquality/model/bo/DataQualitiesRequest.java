package com.miotech.kun.dataquality.model.bo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.common.model.PageInfo;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DataQualitiesRequest extends PageInfo {

    Long gid;
}
