package com.miotech.kun.dataplatform.web.common.deploy.vo;

import lombok.Data;

import java.util.List;

@Data
public class DeployRequest {
    private List<Long> commitIds;
}
