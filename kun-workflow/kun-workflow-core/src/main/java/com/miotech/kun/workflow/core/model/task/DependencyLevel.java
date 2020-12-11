package com.miotech.kun.workflow.core.model.task;

import com.google.common.collect.ImmutableMap;

import java.util.Map;


public enum DependencyLevel {
    WEAK,//弱依赖:上游执行完成即可执行
    STRONG;//强依赖:上游执行过成功才能执行

    private static Map<String,DependencyLevel> levelMaps = ImmutableMap.of(WEAK.name(), WEAK, STRONG.name(), STRONG);

    public static  DependencyLevel resolve(String level) {
        return levelMaps.get(level);
    }
}
