package com.miotech.kun.datadiscovery.model.enums;

public enum GlossaryUserOperation implements UserOperation {
    ADD_GLOSSARY,
    EDIT_GLOSSARY,
    READ_GLOSSARY,
    SEARCH_GLOSSARY,
    REMOVE_GLOSSARY,
    EDIT_GLOSSARY_CHILD,
    COPY_GLOSSARY,
    EDIT_GLOSSARY_RESOURCE,
    EDIT_GLOSSARY_EDITOR,
    ;

    @Override
    public String getName() {
        return name();
    }
}
