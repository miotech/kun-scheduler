package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.UserExtensionInformation;
import org.springframework.stereotype.Service;

/**
 * Default empty implementation
 */
@Service
public class DefaultUserSystem implements UserSystem {

    @Override
    public UserExtensionInformation getExtensionInformation(String username) {
        return null;
    }

}
