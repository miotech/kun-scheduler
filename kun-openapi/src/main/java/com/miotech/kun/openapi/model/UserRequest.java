package com.miotech.kun.openapi.model;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
}
