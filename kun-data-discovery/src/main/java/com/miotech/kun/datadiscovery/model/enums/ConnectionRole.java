package com.miotech.kun.datadiscovery.model.enums;

import com.google.common.collect.ImmutableSet;
import com.miotech.kun.security.common.KunRole;
import com.miotech.kun.security.common.UserOperation;

import java.util.Set;

import static com.miotech.kun.datadiscovery.model.enums.ConnectionUserOperation.*;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-20 15:32
 **/
public enum ConnectionRole implements KunRole {
    CONNECTION_READER(0, ImmutableSet.of(READ_CONN)),
    CONNECTION_USER(1, ImmutableSet.of(READ_CONN, USE_CONN)),
    CONNECTION_MANAGER(2, ImmutableSet.of(READ_CONN, USE_CONN, EDIT_CONN));

    private Integer rank;
    private Set<UserOperation> operations;

    ConnectionRole(Integer rank, Set<UserOperation> operations) {
        this.rank = rank;
        this.operations = operations;
    }

    @Override
    public Set<UserOperation> getUserOperation() {
        return operations;
    }

    @Override
    public Integer rank() {
        return rank;
    }

    @Override
    public String getName() {
        return name();
    }
}
