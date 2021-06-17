package com.miotech.kun.workflow.common.variable.service;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.variable.dao.VariableDao;
import com.miotech.kun.workflow.common.variable.vo.VariableVO;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.variable.Variable;
import com.miotech.kun.workflow.testing.factory.MockVariableFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class VariableServiceTest extends DatabaseTestBase {

    @Inject
    private VariableService variableService;

    @Inject
    private VariableDao variableDao;



    @Test
    public void find_variable_by_key() {
        VariableVO vo = VariableVO.newBuilder()
                .withNamespace("test-name")
                .withKey("test-key")
                .withValue("test-value")
                .build();
        Variable variable = variableService.createVariable(vo);

        Variable result = variableService.find(variable.getFullKey());

        assertThat(result.getNamespace(), is(variable.getNamespace()));
        assertThat(result.getKey(), is(variable.getKey()));
        assertThat(result.getValue(), is(variable.getValue()));
        assertThat(result.isEncrypted(), is(variable.isEncrypted()));
    }

    @Test
    public void get_value_by_key() {
        VariableVO vo = VariableVO.newBuilder()
                .withNamespace("test-name")
                .withKey("test-key")
                .withValue("test-value")
                .build();
        Variable variable = variableService.createVariable(vo);

        String result = variableService.get(variable.getFullKey());

        assertThat(result, is(variable.getValue()));
    }

    @Test
    public void createVariable() {
        VariableVO vo = VariableVO.newBuilder()
                .withNamespace("test-name")
                .withKey("test-key")
                .withValue("test-value")
                .build();
        Variable variable = variableService.createVariable(vo);

        Variable result = variableService.find(variable.getNamespace(), variable.getKey());

        assertThat(result.getNamespace(), is(variable.getNamespace()));
        assertThat(result.getKey(), is(variable.getKey()));
        assertThat(result.getValue(), is(variable.getValue()));
        assertThat(result.isEncrypted(), is(variable.isEncrypted()));
    }

    @Test
    public void createVariable_withoutKey() {
        VariableVO vo = VariableVO.newBuilder()
                .withNamespace("test-name")
                .withValue("test-value")
                .build();
        try {
            Variable variable = variableService.createVariable(vo);
        } catch (Exception e) {
            assertThat(e.getClass(), is(IllegalArgumentException.class));
            assertThat(e.getMessage(), is("Key should not be empty"));
        }
    }

    @Test
    public void createVariable_withoutValue() {
        VariableVO vo = VariableVO.newBuilder()
                .withKey("test-key")
                .build();
        try {
            variableService.createVariable(vo);
            fail();
        } catch (Exception e) {
            assertThat(e.getClass(), is(NullPointerException.class));
            assertThat(e.getMessage(), is("Value should not be `null`"));
        }
    }

    @Test
    public void updateVariable() {
        VariableVO vo = VariableVO.newBuilder()
                .withNamespace("test-name")
                .withKey("test-key")
                .withValue("test-value")
                .build();
        Variable variable = variableService.createVariable(vo);

        VariableVO updated = VariableVO.newBuilder()
                .withNamespace(variable.getNamespace())
                .withKey("test-key")
                .withValue("test-value2")
                .build();
        variableService.updateVariable(updated);
        Variable result = variableService.find(variable.getNamespace(), variable.getKey());

        assertThat(result.getNamespace(), is(variable.getNamespace()));
        assertThat(result.getKey(), is(variable.getKey()));
        assertThat(result.getValue(), is("test-value2"));
        assertThat(result.isEncrypted(), is(variable.isEncrypted()));
    }

    @Test
    public void resolveVariable_withValue() {
        variableDao.create(MockVariableFactory.createVariable()
                .cloneBuilder()
                .withNamespace("test-name")
                .withKey("test")
                .withValue("hello_test")
                .build());

        // with single key
        String result = variableService.resolveVariable("hello_${test-name.test}");
        assertThat(result, is("hello_hello_test"));

        // with multiple key
        variableDao.create(MockVariableFactory.createVariable()
                .cloneBuilder()
                .withNamespace("test-name")
                .withKey("test2")
                .withValue("test2")
                .build());

        result = variableService.resolveVariable("hello_${test-name.test}_${ test-name.test2}");
        assertThat(result, is("hello_hello_test_test2"));
    }

    @Test
    public void renderConfig() {
        variableDao.create(MockVariableFactory.createVariable()
                .cloneBuilder()
                .withNamespace("test-name")
                .withKey("test")
                .withValue("testValue")
                .build());

        Config config = new Config(ImmutableMap
                        .of("testconfig", "hello_${ test-name.test }")
        );
        Config result = variableService.renderConfig(config);
        assertThat(result.getString("testconfig"), is("hello_testValue"));
    }

    @Test
    public void convertVO_withoutEncrypted() {
        VariableVO vo = VariableVO.newBuilder()
                .withNamespace("test-name")
                .withKey("test-key")
                .withValue("test-value")
                .build();
        Variable variable = variableService.createVariable(vo);

        VariableVO result = variableService.convertVO(variable);

        assertThat(result.getNamespace(), is(variable.getNamespace()));
        assertThat(result.getKey(), is(variable.getKey()));
        assertThat(result.getValue(), is(variable.getValue()));
        assertThat(result.isEncrypted(), is(variable.isEncrypted()));
    }

    @Test
    public void convertVO_withEncrypted() {
        VariableVO vo = VariableVO.newBuilder()
                .withNamespace("test-name")
                .withKey("test-key")
                .withValue("test-value")
                .withEncrypted(true)
                .build();
        Variable variable = variableService.createVariable(vo);

        VariableVO result = variableService.convertVO(variable);

        assertThat(result.getNamespace(), is(variable.getNamespace()));
        assertThat(result.getKey(), is(variable.getKey()));
        assertThat(result.getValue(), is("XXXXX"));
        assertThat(result.isEncrypted(), is(variable.isEncrypted()));
    }

    @Test
    public void removeVariable_whenVariableExists_shouldReturnTrue() {
        // 1. Prepare
        VariableVO vo = VariableVO.newBuilder()
                .withNamespace("test-name")
                .withKey("test-key")
                .withValue("test-value")
                .withEncrypted(false)
                .build();
        Variable variable = variableService.createVariable(vo);

        // 2. Process
        boolean removeSuccess = variableService.removeByKey(variable.getFullKey());

        // 3. Validate
        assertTrue(removeSuccess);
        assertNull(variableService.find(variable.getFullKey()));
    }

    @Test
    public void removeVariable_whenVariableNotExists_shouldReturnFalse() {
        // 1. Process
        boolean removeSuccess = variableService.removeByKey("not.existing");

        // 2. Validate
        assertFalse(removeSuccess);
    }
}
