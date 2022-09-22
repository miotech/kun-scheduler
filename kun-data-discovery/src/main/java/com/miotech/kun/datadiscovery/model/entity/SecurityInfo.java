package com.miotech.kun.datadiscovery.model.entity;

import com.google.common.collect.Sets;
import com.miotech.kun.security.common.KunRole;
import com.miotech.kun.datadiscovery.model.enums.SecurityModule;
import com.miotech.kun.security.common.UserOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-05-23 13:38
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityInfo implements Serializable {
    private SecurityModule securityModule;
    private Long sourceSystemId;
    private KunRole kunRole;
    private Set<UserOperation> operations = Sets.newLinkedHashSet();

    public void addUserOperation(UserOperation userOperation) {
        operations.add(userOperation);
    }

    public void addUserOperations(Set<UserOperation> operations) {
        this.operations.addAll(operations);
    }

    public void setOperations(Set<UserOperation> operations) {
        this.operations = Sets.newLinkedHashSet(operations);
    }
}
