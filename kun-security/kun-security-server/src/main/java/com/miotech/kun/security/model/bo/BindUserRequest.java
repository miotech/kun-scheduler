package com.miotech.kun.security.model.bo;

import lombok.Data;

import java.util.List;

@Data
public class BindUserRequest {

    private String module;

    private String rolename;

    private List<String> usernames;

}
