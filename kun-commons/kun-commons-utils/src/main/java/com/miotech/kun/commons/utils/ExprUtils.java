package com.miotech.kun.commons.utils;

import freemarker.template.*;
import com.google.common.base.Strings;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class ExprUtils {

    public static String evalExecuteTimeExpr(String expr, String tick) {
        return expr.replace("${execute_time}", tick);
    }

    public static String evalExpr(String expr, Map<String, Object> args){
        try {
            Template template = new Template("parseTemplate", expr, new Configuration(new Version("2.3.30")));
            StringWriter result = new StringWriter();
            String env = (String) args.getOrDefault("env", "");
            args.put("ref", new UnPack(env));
            template.process(args, result);
            return result.toString();
        }catch (Exception e){
            throw new IllegalArgumentException(e);
        }
    }

    private static class UnPack implements TemplateMethodModelEx {
        private static String env;

        public UnPack(String env_){
            env = env_;
        }

        public String exec(List args) throws TemplateModelException {
            if (args.size() != 1) {
                throw new TemplateModelException("Wrong arguments");
            }
            String dbTable = ((SimpleScalar)args.get(0)).getAsString();
            String db = dbTable.split("\\.")[0];
            String table = dbTable.split("\\.")[1];
            String resolvedDb = ("prod".equals(env) || Strings.isNullOrEmpty(env)) ? db: db + "_" + env;
            return resolvedDb + "." + table;
        }

    }

}
