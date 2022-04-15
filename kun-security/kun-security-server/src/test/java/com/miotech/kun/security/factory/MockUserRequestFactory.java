package com.miotech.kun.security.factory;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.security.model.bo.UserRequest;

public class MockUserRequestFactory {

    private MockUserRequestFactory() {
    }

    public static UserRequest create() {
        return UserRequest.builder()
                .id(IdGenerator.getInstance().nextId())
                .username("admin")
                .password("admin")
                .firstName("admin")
                .lastName("admin")
                .email("test@email.com")
                .weComId("123")
                .createUser(1L)
                .createTime(System.currentTimeMillis())
                .updateUser(1L)
                .updateTime(System.currentTimeMillis())

                .build();

    }

}
