package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.UserExtensionInformation;

public interface UserSystem {

    UserExtensionInformation getExtensionInformation(String username);

}
