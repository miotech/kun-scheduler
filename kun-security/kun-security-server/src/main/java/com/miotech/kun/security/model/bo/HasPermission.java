package com.miotech.kun.security.model.bo;

import com.miotech.kun.security.model.constant.PermissionType;
import lombok.Builder;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2021/1/25
 */
@Data
@Builder
public class HasPermission {

    Long permissionId;

    Long userId;

    Long resourceId;

    PermissionType permissionType;

}
