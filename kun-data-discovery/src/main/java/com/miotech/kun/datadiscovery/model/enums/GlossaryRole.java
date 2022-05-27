package com.miotech.kun.datadiscovery.model.enums;

import com.google.common.collect.ImmutableSet;
import com.miotech.kun.security.common.KunRole;
import com.miotech.kun.security.common.UserOperation;

import java.util.Set;

import static com.miotech.kun.datadiscovery.model.enums.GlossaryUserOperation.*;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-05-23 11:44
 **/

public enum GlossaryRole implements KunRole {
    ANONYMOUS_USER(0, ImmutableSet.of()),
    GLOSSARY_VIEWER(1, ImmutableSet.of(READ_GLOSSARY, SEARCH_GLOSSARY)),
    GLOSSARY_EDITOR(2, ImmutableSet.of(EDIT_GLOSSARY, READ_GLOSSARY, SEARCH_GLOSSARY, REMOVE_GLOSSARY,
            EDIT_GLOSSARY_CHILD, COPY_GLOSSARY, PASTE_GLOSSARY, EDIT_GLOSSARY_RESOURCE)),
    GLOSSARY_MANAGER(3, ImmutableSet.of(ADD_GLOSSARY, EDIT_GLOSSARY, READ_GLOSSARY, SEARCH_GLOSSARY, REMOVE_GLOSSARY,
            EDIT_GLOSSARY_CHILD, COPY_GLOSSARY, EDIT_GLOSSARY_RESOURCE, EDIT_GLOSSARY_EDITOR));


    private Set<UserOperation> operations;
    private Integer rank;

    GlossaryRole(Integer rank, Set<UserOperation> operations) {
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
