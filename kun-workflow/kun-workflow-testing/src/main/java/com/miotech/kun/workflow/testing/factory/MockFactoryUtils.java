package com.miotech.kun.workflow.testing.factory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MockFactoryUtils {
    public static <T> List<T> selectItems(List<T> items, List<Integer> idx) {
        if (idx == null) {
            return Collections.emptyList();
        }
        return idx.stream().map(items::get).collect(Collectors.toList());
    }

    public static Map<Integer, List<Integer>> parseRelations(String relations) {
        Map<Integer, List<Integer>> res = Maps.newHashMap();
        Splitter.on(";").trimResults().splitToList(relations).forEach(e -> {
            int i = e.indexOf(">>");
            if (i == -1) {
                return;
            }
            int from = Integer.parseInt(e.substring(0, i));
            int to = Integer.parseInt(e.substring(i + 2));
            if (!res.containsKey(to)) {
                res.put(to, Lists.newArrayList(from));
            } else {
                res.get(to).add(from);
            }
        });
        return res;
    }

}
