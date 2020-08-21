package com.miotech.kun.commons.web.mock;

import java.util.Map;

public class MockObject {
    private Long id;

    private Map<String, Object> config;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
