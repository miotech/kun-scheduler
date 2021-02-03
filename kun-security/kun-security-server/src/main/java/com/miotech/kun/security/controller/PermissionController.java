package com.miotech.kun.security.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.model.vo.IdVO;
import com.miotech.kun.security.model.bo.PermissionRequest;
import com.miotech.kun.security.model.entity.Permission;
import com.miotech.kun.security.model.entity.Permissions;
import com.miotech.kun.security.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Jie Chen
 * @created: 2021/1/19
 */
@RestController
@RequestMapping("/kun/api/v1/security/permission")
public class PermissionController {

    @Autowired
    PermissionService permissionService;

    @PostMapping("/save")
    public RequestResult<Permissions> savePermission(@RequestBody PermissionRequest permissionRequest) {
        return RequestResult.success(permissionService.savePermission(permissionRequest));
    }

    @PostMapping("/remove/{id}")
    public RequestResult<IdVO> removePermission(@PathVariable("id") Long id) {
        permissionService.removePermission(id);
        IdVO idVO = new IdVO();
        idVO.setId(id);
        return RequestResult.success(idVO);
    }
}
