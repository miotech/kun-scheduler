package com.miotech.kun.workflow.executor.local;

import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.resource.Resource;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OperatorContextImpl implements OperatorContext {
    private final Map<String, Variable> variables;
    private final Map<String, Param> parameters;
    private final Logger logger;

    public OperatorContextImpl(TaskAttempt attempt, Resource logResource) {
        this.variables = buildVariables(attempt.getTaskRun().getVariables());
        this.parameters = buildParams(attempt.getTaskRun().getTask().getArguments());
        this.logger = buildLogger(attempt, logResource);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getParameter(String name) {
        if (parameters.containsKey(name)) {
            return parameters.get(name).getValue();
        } else {
            return null;
        }
    }

    @Override
    public String getVariable(String name) {
        if (variables.containsKey(name)) {
            String val = variables.get(name).getValue();
            return val != null ? val : variables.get(name).getDefaultValue();
        } else {
            return null;
        }
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private Logger buildLogger(TaskAttempt attempt, Resource logResource) {
        return new OperatorLogger(attempt.getId(), logResource);
    }

    private Map<String, Variable> buildVariables(List<Variable> variables) {
        return variables.stream()
                .collect(Collectors.toMap(Variable::getKey, Function.identity()));
    }

    private Map<String, Param> buildParams(List<Param> params) {
        return params.stream()
                .collect(Collectors.toMap(Param::getName, Function.identity()));
    }
}
