package com.miotech.kun.security.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleScope {

    private String role;

    private String sourceSystemId;

}
