package com.miotech.kun.security.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2021/1/20
 */
@Data
public class Permissions {

    List<Permission> permissions = new ArrayList<>();

    public void add(Permission permission) {
        permissions.add(permission);
    }
}
