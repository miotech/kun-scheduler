package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.ResourceRequest;
import com.miotech.kun.security.model.entity.Resource;
import com.miotech.kun.security.persistence.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Chen
 * @created: 2021/1/18
 */
@Service
public class ResourceService extends BaseSecurityService {

    @Autowired
    ResourceRepository resourceRepository;

    public Resource addResource(ResourceRequest resourceRequest) {
        resourceRequest.setCreateUser(getCurrentUsername());
        resourceRequest.setUpdateUser(getCurrentUsername());
        resourceRequest.setCreateTime(System.currentTimeMillis());
        resourceRequest.setUpdateTime(System.currentTimeMillis());
        return resourceRepository.addResource(resourceRequest);
    }

    public Long removeResource(Long id) {
        return resourceRepository.removeResource(id);
    }
}
