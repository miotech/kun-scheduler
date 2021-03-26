package com.miotech.kun.security.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AuthenticationOriginInfo implements Serializable {

    String authType;

    List<String> groups;

}
