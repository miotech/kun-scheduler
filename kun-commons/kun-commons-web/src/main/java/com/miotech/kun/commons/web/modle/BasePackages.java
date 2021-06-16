package com.miotech.kun.commons.web.modle;

import java.util.List;

public class BasePackages {

    private final List<String> basePackages;

    public BasePackages(List<String> basePackages){
        this.basePackages = basePackages;
    }

    public List<String> getBasePackages() {
        return basePackages;
    }
}
