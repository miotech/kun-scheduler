package com.miotech.kun.security.model.constant;

/**
 * @author: Jie Chen
 * @created: 2021/1/18
 */
public enum PermissionType {

    ADMIN(0),

    WRITE(1),

    READ(2);

    private final int code;
    PermissionType(int code) {
        this.code = code;
    }
}
