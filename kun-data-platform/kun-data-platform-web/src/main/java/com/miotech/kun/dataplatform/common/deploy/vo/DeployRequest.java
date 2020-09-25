package com.miotech.kun.dataplatform.common.deploy.vo;

import lombok.Data;

import java.util.List;

@Data
public class DeployRequest {
    private List<Long> commitIds;
}
