package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.ResourceRequest;
import com.miotech.kun.security.model.entity.Resource;
import com.miotech.kun.security.persistence.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Jie Chen
 * @created: 2021/1/18
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceService extends BaseSecurityService {

    @Autowired
    ResourceRepository resourceRepository;

    public Resource findByName(String resourceName) {
        return resourceRepository.findByName(resourceName);
    }

    public Resource addResource(ResourceRequest resourceRequest) {
        resourceRequest.setCreateUser(getCurrentUser().getId());
        resourceRequest.setUpdateUser(getCurrentUser().getId());
        resourceRequest.setCreateTime(System.currentTimeMillis());
        resourceRequest.setUpdateTime(System.currentTimeMillis());
        return resourceRepository.addResource(resourceRequest);
    }

    public Long removeResource(Long id) {
        return resourceRepository.removeResource(id);
    }
}
