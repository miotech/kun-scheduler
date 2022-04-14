package com.miotech.kun.commons.utils.model;

import java.util.Objects;

public class TestClass {
    private String name;
    private Long Id;
    private String describe;

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        Id = id;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return Id;
    }

    public String getDescribe() {
        return describe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestClass testClass = (TestClass) o;
        return Objects.equals(name, testClass.name) && Objects.equals(Id, testClass.Id) && Objects.equals(describe, testClass.describe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Id, describe);
    }
}
