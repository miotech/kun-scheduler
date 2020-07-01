package com.miotech.kun.metadata.databuilder.schedule;

import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.resource.Resource;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockOperatorContextImpl implements OperatorContext {

    private Map<String, String> params = new HashMap();
    private Map<String, String> vars = new HashMap();

    public void setVar(String key, String value) {
        this.vars.put(key, value);
    }

    public void setVars(Map<String, String> params) {
        this.vars.putAll(params);
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public String getParameter(String name){
        return params.getOrDefault(name, "");
    }

    @Override
    public String getVariable(String name){
        return vars.getOrDefault(name, "");
    }

    @Override
    public Logger getLogger() {
        return new TestOperatorLogger();
    }

    public void report(List<DataStore> inlets, List<DataStore> outlets){
        System.out.println("printing lineage result");
        System.out.println(inlets);
        System.out.println(outlets);
    }

    private class TestOperatorLogger implements Logger {

        private final org.slf4j.Logger logger = LoggerFactory.getLogger(TestOperatorLogger.class);
        @Override
        public void debug(String format, Object... arguments) {
            logger.debug(format, arguments);
        }

        @Override
        public void info(String format, Object... arguments) {
            logger.info(format, arguments);
        }

        @Override
        public void warn(String format, Object... arguments) {
            logger.warn(format, arguments);
        }

        @Override
        public void error(String format, Object... arguments) {
            logger.error(format, arguments);
        }

    }

}
