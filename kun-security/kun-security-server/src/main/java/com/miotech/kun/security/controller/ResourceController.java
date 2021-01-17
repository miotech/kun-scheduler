package com.miotech.kun.security.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.model.vo.IdVO;
import com.miotech.kun.security.model.bo.ResourceRequest;
import com.miotech.kun.security.model.entity.Resource;
import com.miotech.kun.security.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Jie Chen
 * @created: 2021/1/18
 */
@RestController
@RequestMapping("/kun/api/v1/security/resource")
public class ResourceController {

    @Autowired
    ResourceService resourceService;

    @PostMapping("/add")
    public RequestResult<Resource> addResource(@RequestBody ResourceRequest resourceRequest) {
        return RequestResult.success(resourceService.addResource(resourceRequest));
    }

    @PostMapping("/remove/{id}")
    public RequestResult<IdVO> removeResource(@PathVariable("id") Long id) {
        resourceService.removeResource(id);
        IdVO idVO = new IdVO();
        idVO.setId(id);
        return RequestResult.success(idVO);
    }
}
