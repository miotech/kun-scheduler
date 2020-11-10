package com.miotech.kun.security.facade;

import com.miotech.kun.security.model.bo.UserInfo;

/**
 * Exposed RPC service interface of security service module.
 * @author: Jie Chen
 * @created: 2020/11/10
 */
public interface SecurityServiceFacade {

    /**
     * Get user info by id
     * @param id user id
     * @return User info object. Returns null if not found by id.
     */
    UserInfo getUserById(Long id);
}
