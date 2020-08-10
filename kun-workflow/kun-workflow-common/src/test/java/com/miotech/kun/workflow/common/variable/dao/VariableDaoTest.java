package com.miotech.kun.workflow.common.variable.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.core.model.variable.Variable;
import com.miotech.kun.workflow.testing.factory.MockVariableFactory;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VariableDaoTest extends DatabaseTestBase {

    @Inject
    private VariableDao variableDao;

    @Test
    public void fetchByKey() {
        Variable variable = MockVariableFactory.createVariable();
        variableDao.create(variable);

        Variable result = variableDao.fetchByKey(variable.getFullKey()).get();

        assertThat(result.getNamespace(), is(variable.getNamespace()));
        assertThat(result.getKey(), is(variable.getKey()));
        assertThat(result.getValue(), is(variable.getValue()));
        assertThat(result.isEncrypted(), is(variable.isEncrypted()));
    }

    @Test
    public void fetchAll() {
        for( int i =0 ; i< 100; i++) {
            Variable  variable = MockVariableFactory.createVariable();

            variableDao.create(variable.cloneBuilder()
                    .withKey(variable.getKey() + i)
                    .withKey(variable.getValue() + i)
                    .build()
            );
        }

        List<Variable> result = variableDao.fetchAll();

        assertThat(result.size(), is(100));
    }

    @Test
    public void fetchByPrefix() {
        List<Variable> variables = MockVariableFactory.createVariables(3, "test-key", "test-value",false);
        StringBuilder prefix = new StringBuilder();
        for( int i = 1; i <= 3; i++) {
            Variable variable = variables.get(i-1);
            prefix.append("key").append(i).append(".");
            variableDao.create(variable.cloneBuilder()
                    .withNamespace("namespace")
                    .withKey(prefix + variable.getKey())
                    .build()
            );
        }

        List<Variable> result = variableDao.fetchByPrefix("namespace");
        assertThat(result.size(), is(3));

        result = variableDao.fetchByPrefix("namespace.key1.key2");
        assertThat(result.size(), is(2));


        result = variableDao.fetchByPrefix("namespace.key1.key2.key3");
        assertThat(result.size(), is(1));
    }

    @Test
    public void update() {
        Variable  variable = MockVariableFactory.createVariable();
        variableDao.create(variable);

        variableDao.update(variable.cloneBuilder()
                .withValue(variable.getValue() + "_changed")
                .build());

        Variable result = variableDao.fetchByKey(variable.getFullKey()).get();

        assertThat(result.getNamespace(), is(variable.getNamespace()));
        assertThat(result.getKey(), is(variable.getKey()));
        assertThat(result.getValue(), is(variable.getValue()+ "_changed"));
        assertThat(result.isEncrypted(), is(variable.isEncrypted()));
    }
}