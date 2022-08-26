package com.miotech.kun.metadata.core.model.filter;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public enum FilterRuleType {
    EMPTY,
    MCE,
    MSE;

    public static String ruleGenerate(List<ImmutablePair<String, String>> ruleParams) {
        StringJoiner stringJoiner = new StringJoiner("/", "/", "/");
        ruleParams.forEach(pair -> stringJoiner.add(String.format("%s:%s", pair.getKey(), pair.getValue())));
        return stringJoiner.toString();
    }

    public static String mseRule(String type, String datasource, String database, String name) {
        return mceRule(type, datasource, database, name);

    }

    public static String mceRule(String type, String datasource, String database, String name) {
        List<ImmutablePair<String, String>> pairList = new ArrayList<>();
        pairList.add(new ImmutablePair<>("type", type));
        pairList.add(new ImmutablePair<>("datasource", datasource));
        pairList.add(new ImmutablePair<>("database", database));
        pairList.add(new ImmutablePair<>("name", name));
        return ruleGenerate(pairList);
    }
}
