package com.miotech.kun.security.model.bo;

import com.miotech.kun.security.model.constant.EntityType;
import com.miotech.kun.security.model.constant.PermissionType;
import lombok.Builder;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2021/1/25
 */
@Data
@Builder
public class HasPermissionRequest {

    Long permissionId;

    Long subjectId;

    EntityType subjectType;

    Long objectId;

    EntityType objectType;

    PermissionType permissionType;

}
