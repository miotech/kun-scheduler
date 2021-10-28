package com.miotech.kun.commons.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import freemarker.template.*;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

public class ExprUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExprUtils.class);

    public static String evalExecuteTimeExpr(String expr, String tick) {
        return evalExpr(expr, ImmutableMap.of("execute_time", tick));
    }

    public static String evalExpr(String expr, Map<String, Object> dataModel) {
        try {
            Template template = new Template("parseTemplate", expr, new Configuration(new Version("2.3.30")));
            StringWriter result = new StringWriter();
            template.process(addBuiltinMethods(dataModel), result);
            logger.info("expr resolved to: " + result);
            return result.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Map<String, Object> addBuiltinMethods(Map<String, Object> dataModel) {
        return ImmutableMap.<String, Object>builder()
                .putAll(dataModel)
                .put("ref", new RefMethod(dataModel))
                .build();
    }

    private static class RefMethod implements TemplateMethodModelEx {
        private Map<String, Object> dataModel;

        public RefMethod(Map<String, Object> dataModel) {
            this.dataModel = dataModel;
        }

        @Override
        public String exec(List arguments) throws TemplateModelException {
            String targetName = (String) dataModel.get("target.schema");

            // length = 1
            String arg1 = ((SimpleScalar) arguments.get(0)).getAsString();
            String[] dbAndTable = arg1.split("\\.");
            String db = dbAndTable[0];
            String resolvedDb = "prod".equals(targetName) || Strings.isNullOrEmpty(targetName) ? db : db + "_" + targetName;
            String table = dbAndTable[1];
            String resolvedDbTable = resolvedDb + "." + table;

            // length = 2
            List<String> vars = new ArrayList<>();
            vars.add(resolvedDbTable);
            vars.addAll((List<String>) arguments.subList(1, arguments.size())
                    .stream()
                    .map(x ->  dataModel.get(((SimpleScalar)x).getAsString()))
                    .collect(Collectors.toList()));

            return String.join("_", vars);
        }
    }
}
