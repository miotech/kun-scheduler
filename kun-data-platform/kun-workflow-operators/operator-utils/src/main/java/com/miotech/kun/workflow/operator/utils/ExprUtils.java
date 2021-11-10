package com.miotech.kun.workflow.operator.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import freemarker.template.*;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.*;
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
        Target target = dataModel.containsKey("target") ? Target.fromExecuteTarget((ExecuteTarget) dataModel.get("target")) : new Target();

        Map<String, Object> newDataModel = new HashMap<>();
        newDataModel.putAll(dataModel);
        newDataModel.put("target", target);
        newDataModel.put("ref", new RefMethod(target));
        newDataModel.put("suffix", new SuffixMethod());
        return newDataModel;
    }

    private static class SuffixMethod implements TemplateMethodModelEx {
        @Override
        public String exec(List arguments) throws  TemplateModelException{
            List<String> vars = new ArrayList<>();
            vars.addAll((Collection<? extends String>) arguments
                    .stream()
                    .map(x ->  ((SimpleScalar)x).getAsString())
                    .filter(x -> !Strings.isNullOrEmpty((String) x))
                    .collect(Collectors.toList()));

            return vars.isEmpty() ? "" : "_" + String.join("_", vars);
        }
    }

    private static class RefMethod implements TemplateMethodModelEx {
        private Target target;
        public RefMethod(Target t){
            target = t;
        }

        @Override
        public String exec(List arguments) throws TemplateModelException {
            String targetSchema = target.getSchema();

            // length = 1
            String arg1 = ((SimpleScalar) arguments.get(0)).getAsString();
            String[] dbAndTable = arg1.split("\\.");
            String db = dbAndTable[0];
            String resolvedDb = Strings.isNullOrEmpty(targetSchema) ? db : db + "_" + targetSchema;
            String table = dbAndTable[1];
            String resolvedDbTable = resolvedDb + "." + table;

//            // length = 2
            List<String> vars = new ArrayList<>();
            vars.add(resolvedDbTable);
            vars.addAll((List<String>) arguments.subList(1, arguments.size())
                    .stream()
                    .map(x ->  ((SimpleScalar)x).getAsString())
                    .filter(x -> !Strings.isNullOrEmpty((String) x))
                    .collect(Collectors.toList()));

            return String.join("_", vars);
        }
    }

}
