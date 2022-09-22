package com.miotech.kun.datadiscovery.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.datadiscovery.model.entity.SecurityInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-20 15:49
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ConnectionInfoSecurityVO extends ConnectionInfoVO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SecurityInfo securityInfo;
    private List<String> securityUserList; //All users if not specified
}
