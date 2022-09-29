package com.miotech.kun.openapi.model.request;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
}
