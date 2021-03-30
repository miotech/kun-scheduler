package com.miotech.kun.security.model;

import com.miotech.kun.security.model.bo.UserRequest;
import org.springframework.stereotype.Component;

/**
 * @author: Jie Chen
 * @created: 2021/3/30
 */
@Component
public class ModelConverter {

    public UserRequest convert2UserRequest(UserInfo userInfo) {
        return UserRequest.builder()
                .id(userInfo.getId())
                .username(userInfo.getUsername())
                .password(userInfo.getPassword())
                .authOriginInfo(userInfo.getAuthOriginInfo())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .email(userInfo.getEmail())
                .build();
    }
}
