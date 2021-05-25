package com.miotech.kun.workflow.common.variable.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.variable.dao.VariableDao;
import com.miotech.kun.workflow.common.variable.vo.VariableVO;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.variable.Variable;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class VariableService {
    private static final String ENCRYPTED_VALUE_MASK = "XXXXX";
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("(\\$\\{\\s*([^\\}\\s]+)\\s*\\})");

    @Inject
    private VariableDao variableDao;

    public List<Variable> findAll() {
        return variableDao.fetchAll();
    }

    public Variable find(String namespace, String key) {
        return find(buildKey(namespace, key));
    }

    public Variable find(String key) {
        return variableDao.fetchByKey(key).orElse(null);
    }

    public boolean removeByKey(String key) {
        return variableDao.removeByKey(key);
    }

    public String get(String key) {
        Variable variable = find(key);
        return variable == null ? "" : find(key).getValue();
    }

    public Map<String, String> getVariables(String prefix) {
        List<Variable> variableList = variableDao.fetchByPrefix(prefix);
        if (variableList.isEmpty()) {
            throw new IllegalArgumentException(String.format("Cannot find any variable with prefix: \"%s\"", prefix));
        }
        return variableList
                .stream()
                .collect(Collectors.toMap(Variable::getKey, Variable::getValue));
    }

    public Variable createVariable(VariableVO variableVO ) {
        validateVariableVO(variableVO);
        String key = buildKey(variableVO.getNamespace(), variableVO.getKey());
        variableDao.fetchByKey(key)
                .ifPresent(var -> {
                        throw new IllegalArgumentException(String.format("Variable with key \"%s\" has existed", key));
                });
        Variable var = Variable.newBuilder()
                .withNamespace(variableVO.getNamespace())
                .withKey(variableVO.getKey())
                .withValue(variableVO.getValue())
                .withEncrypted(variableVO.isEncrypted())
                .build();
        variableDao.create(var);
        return var;
    }

    public Variable updateVariable(VariableVO variableVO ) {
        validateVariableVO(variableVO);
        Variable var = find(variableVO.getNamespace(), variableVO.getKey())
                .cloneBuilder()
                .withKey(variableVO.getKey())
                .withValue(variableVO.getValue())
                .withEncrypted(variableVO.isEncrypted())
                .build();
        variableDao.update(var);
        return var;
    }

    public Config renderConfig(Config config) {
            Map<String, Object> replaced = resolveVariablesInMap(config.getValues());
            return new Config(replaced);
    }

    public String resolveVariable(String strWithVar) {
        final Matcher matcher = VARIABLE_PATTERN.matcher(strWithVar);
        String result = strWithVar;
        while (matcher.find()) {
            for (int i = 1; i < matcher.groupCount(); i = i + 2) {
                String fullMatch = matcher.group(i);
                String variableKey = matcher.group(i + 1);
                result = result.replace(fullMatch, get(variableKey));
            }
        }
        return result;
    }

    public Map<String, Object> resolveVariablesInMap(Map<String, Object> values) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, resolveVariable((String) value));
            } else if (value instanceof Map) {
                result.put(key, resolveVariablesInMap((Map<String,Object>) value));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    public VariableVO convertVO(Variable var) {
        return VariableVO.newBuilder()
                .withNamespace(var.getNamespace())
                .withKey(var.getKey())
                .withValue(var.isEncrypted()? ENCRYPTED_VALUE_MASK: var.getValue())
                .withEncrypted(var.isEncrypted())
                .build();
    }

    private String buildKey(String... keys) {
        return String.join(".", keys);
    }

    private Boolean validateVariableVO(VariableVO vo) {
        Preconditions.checkNotNull(StringUtils.isNoneEmpty(vo.getNamespace()), "variable namespace should not be `null`");
        String key = vo.getKey();
        Preconditions.checkArgument(StringUtils.isNoneEmpty(key), "Key should not be empty");
        Preconditions.checkNotNull(vo.getValue(), "Value should not be `null`");
        return true;
    }
}
