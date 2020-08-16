package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.workflow.core.model.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class MockVariableFactory {

    private MockVariableFactory() {}

    public static Variable createVariable() {
        return createVariables(1,"test-key", "test-value", true).get(0);
    }

    public static List<Variable> createVariables(int num, String key, String value, boolean isEncrypted) {
        List<Variable> result = new ArrayList<>();
        String[] keys = key.split("\\.");
        String targetKey = key;
        String targetNamespace = "test";
        if (keys.length > 1) {
            targetKey = keys[1];
            targetNamespace = keys[2];
        }
        for( int i =0; i < num; i++) {
            result.add(Variable.newBuilder()
                    .withNamespace(targetNamespace)
                    .withKey(targetKey)
                    .withValue(value)
                    .withEncrypted(isEncrypted)
                    .build());
        }
        return result;
    }
}
