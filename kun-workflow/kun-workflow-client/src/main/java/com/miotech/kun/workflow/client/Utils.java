package com.miotech.kun.workflow.client;

import com.google.common.base.Preconditions;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Variable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    private Utils() {}

    public static List<Param> mapToParams(Map<String, String>  map) {
        Preconditions.checkNotNull(map);
        return map.entrySet().stream()
                .map(x -> Param.newBuilder()
                        .withName(x.getKey())
                        .withValue(x.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<Variable> mapToVariables(Map<String, String>  map) {
        Preconditions.checkNotNull(map);
        return map.entrySet().stream()
                .map(x -> Variable.newBuilder()
                        .withKey(x.getKey())
                        .withValue(x.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
