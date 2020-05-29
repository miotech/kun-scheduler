package com.miotech.kun.workflow.common.operator.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.exception.NameConflictException;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.exception.RuleOperatorInUseException;
import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.*;

public class OperatorServiceTest extends DatabaseTestBase {
    @Inject
    private OperatorService operatorService;

    @Inject
    private TaskDao taskDao;

    private OperatorPropsVO convertOperatorToPropertyVO(Operator operator) {
        return OperatorPropsVO.newBuilder()
                .withName(operator.getName())
                .withDescription(operator.getDescription())
                .withParams(operator.getParams())
                .withPackagePath(operator.getPackagePath())
                .withClassName(operator.getClassName())
                .build();
    }

    private OperatorPropsVO generateExampleOperatorPropsVO() {
        return OperatorPropsVO.newBuilder()
                .withName("BashOperator")
                .withDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                .withClassName("com.miotech.kun.example.BashOperator")
                .withPackagePath("s3://storage.miotech.com")
                .withParams(new ArrayList<>())
                .build();
    }

    public void assertFailToCreateOperatorWithException(OperatorPropsVO vo, Class<?> exceptionClazz) {
        try {
            operatorService.createOperator(vo);
            // if exception is not thrown, case should fail
            fail();
        } catch (Exception e) {
            // should thrown DuplicatedNameException
            assertThat(e, instanceOf(exceptionClazz));
        }
    }

    @Test
    public void createOperator_withProperInfoVO_shouldSuccess() {
        // Prepare
        // 1. initialize a operator info VO
        OperatorPropsVO operatorPropsVO = generateExampleOperatorPropsVO();

        // Process
        // 2. persist it with service interface
        Operator persistedOperator = operatorService.createOperator(operatorPropsVO);

        // Validate
        // 3. should success
        OperatorPropsVO persistedOperatorProperties = convertOperatorToPropertyVO(persistedOperator);
        assertThat(operatorPropsVO, samePropertyValuesAs(persistedOperatorProperties));
    }

    @Test
    public void createOperator_withDuplicatedOperatorName_shouldFail() {
        // Prepare
        // 1. initialize an operator properties VO
        OperatorPropsVO operatorPropsVO = generateExampleOperatorPropsVO();

        // 2. prepare another operator properties VO with same name
        OperatorPropsVO operatorPropsVOWithDuplicatedName = operatorPropsVO.cloneBuilder()
                // with exact same name...
                .withName("BashOperator")
                // and different properties
                .withDescription("Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
                .withClassName("com.miotech.kun.example.BashOperator2")
                .withPackagePath("s3://storage2.miotech.com")
                .build();

        // Process & Validate
        // 3. Insert first operator
        operatorService.createOperator(operatorPropsVO);
        // 4. Insert second operator with same name
        assertFailToCreateOperatorWithException(operatorPropsVOWithDuplicatedName, NameConflictException.class);
    }

    @Test
    public void createOperator_withInvalidVO_shouldFail() {
        // Prepare
        // case 1: Empty name
        OperatorPropsVO voWithEmptyName = generateExampleOperatorPropsVO().cloneBuilder()
                .withName("")
                .build();
        // case 2: Null description
        OperatorPropsVO voWithNullDescription = generateExampleOperatorPropsVO().cloneBuilder()
                .withDescription(null)
                .build();
        // case 3: Empty case name
        OperatorPropsVO voWithEmptyClassName = generateExampleOperatorPropsVO().cloneBuilder()
                .withClassName("")
                .build();
        // case 4: Empty package name
        OperatorPropsVO voWithEmptyPackagePath = generateExampleOperatorPropsVO().cloneBuilder()
                .withPackagePath("")
                .build();

        // Process & Validate
        assertFailToCreateOperatorWithException(voWithEmptyName, IllegalArgumentException.class);
        assertFailToCreateOperatorWithException(voWithNullDescription, IllegalArgumentException.class);
        assertFailToCreateOperatorWithException(voWithEmptyClassName, IllegalArgumentException.class);
        assertFailToCreateOperatorWithException(voWithEmptyPackagePath, IllegalArgumentException.class);
    }

    @Test
    public void saveOperator_withNewOperator_shouldPerformCreateAction() {
        // Prepare
        // 1. operator list should be empty
        List<Operator> preActionOperators = operatorService.getAllOperators();
        assertEquals(0, preActionOperators.size());

        // 2. initialize an operator and save it
        OperatorPropsVO operatorPropsVO = generateExampleOperatorPropsVO();
        Operator operator = Operator.newBuilder()
                .withName(operatorPropsVO.getName())
                .withDescription(operatorPropsVO.getDescription())
                .withParams(operatorPropsVO.getParams())
                .withPackagePath(operatorPropsVO.getPackagePath())
                .withClassName(operatorPropsVO.getClassName())
                .build();

        // Process
        // 3. Save operator
        operatorService.saveOperator(operator);

        // Validate
        // 4. A new operator should be created
        List<Operator> postActionOperators = operatorService.getAllOperators();
        assertEquals(1, postActionOperators.size());

        // 5. ...with properties assigned properly (except `id`)
        Operator persistedOperator = postActionOperators.get(0);
        assertThat(persistedOperator, samePropertyValuesAs(operator, "id"));
    }

    @Test
    public void saveOperator_withExistedOperator_shouldPerformUpdateAction() {
        // Prepare
        // 1. create an operator
        OperatorPropsVO operatorPropsVO = generateExampleOperatorPropsVO();
        Operator operator = operatorService.createOperator(operatorPropsVO);
        assertEquals("BashOperator", operator.getName());

        // 2. operator list should contain only one item
        List<Operator> preActionOperators = operatorService.getAllOperators();
        assertEquals(1, preActionOperators.size());

        // Process
        // 3. edit properties
        Operator editedOperator = operator.cloneBuilder()
                .withName("ShellOperator")
                .withClassName("com.miotech.kun.example.ShellOperator")
                .build();

        // 4. save operator
        operatorService.saveOperator(editedOperator);

        // Validate
        // 5. should not create another operator
        List<Operator> postActionOperators = operatorService.getAllOperators();
        assertEquals(1, postActionOperators.size());

        // 6. operator should be updated
        Operator updatedOperator = postActionOperators.get(0);
        assertEquals("ShellOperator", updatedOperator.getName());
        assertThat(updatedOperator, samePropertyValuesAs(editedOperator));
    }

    @Test
    public void saveOperator_withConflictName_shouldThrowException() {
        // Prepare
        // 1. create an operator named "BashOperator"
        OperatorPropsVO bashOperatorPropsVO = generateExampleOperatorPropsVO();
        Operator bashOperator = operatorService.createOperator(bashOperatorPropsVO);

        // 2. create another operator named "ShellOperator"
        OperatorPropsVO shellOperatorPropsVO = bashOperatorPropsVO.cloneBuilder()
                .withName("ShellOperator")
                .withClassName("com.miotech.kun.example.ShellOperator")
                .build();
        operatorService.createOperator(shellOperatorPropsVO);

        // Process
        // 3. Try to rename `BashOperator` to `ShellOperator` and save
        Operator renamedBashOperator = bashOperator.cloneBuilder().withName("ShellOperator").build();

        try {
            operatorService.saveOperator(renamedBashOperator);
            fail();
        } catch (Exception e) {
            // Validate
            // 4. should throw `DuplicatedNameException`
            assertThat(e, instanceOf(NameConflictException.class));
        }
    }

    @Test
    public void fullUpdateOperator() {
        // TODO: implement this
    }

    @Test
    public void partialUpdateOperator_withExistingOperator_shouldSuccess() {
        // Prepare
        // 1. create an operator
        OperatorPropsVO operatorPropsVO = MockOperatorFactory.createOperatorPropsVO();
        Operator operator = operatorService.createOperator(operatorPropsVO);

        // 2. prepare an operator value object with only part of its props assigned
        OperatorPropsVO updateVO = OperatorPropsVO.newBuilder()
                .withName("ShellOperator")
                .build();

        System.out.println(JSONUtils.toJsonString(updateVO));

        // Process
        // 3. perform partial update
        operatorService.partialUpdateOperator(operator.getId(), updateVO);

        // Validate
        Optional<Operator> updatedOperatorOptional = operatorService.fetchOperatorById(operator.getId());
        assertTrue(updatedOperatorOptional.isPresent());
        Operator updatedOperator = updatedOperatorOptional.get();
        // 4. updated operator should have same property values as original one, except updated properties
        assertThat(updatedOperator, samePropertyValuesAs(operator, "name"));
        assertThat(updatedOperator.getName(), is("ShellOperator"));
    }

    @Test
    public void partialUpdateOperator_withNonExistingId_shouldThrowException() {
        // Prepare
        // 1. prepare an operator value object with only part of its props assigned
        OperatorPropsVO updateVO = OperatorPropsVO.newBuilder()
                .withName("ShellOperator")
                .build();

        // 2. perform partial update on a non-exist ID
        try {
            operatorService.partialUpdateOperator(1L, updateVO);
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }
    }

    @Test
    public void deleteOperator_withExistedIdAndNoTaskReferences_shouldSuccess() {
        // Prepare
        // 1. create an operator
        OperatorPropsVO operatorPropsVO = MockOperatorFactory.createOperatorPropsVO();
        Operator operator = operatorService.createOperator(operatorPropsVO);

        // Process
        // 2. delete operator by id
        operatorService.deleteOperator(operator);

        // Validate
        Optional<Operator> operatorOptional = operatorService.fetchOperatorById(operator.getId());
        assertFalse(operatorOptional.isPresent());
    }

    @Test
    public void deleteOperator_withTasksUsage_shouldThrowRuleOperatorInUseException() {
        // Prepare
        // 1. create an operator
        OperatorPropsVO operatorPropsVO = MockOperatorFactory.createOperatorPropsVO();
        Operator operator = operatorService.createOperator(operatorPropsVO);
        // 2. create a task and use the operator we created
        Task task = MockTaskFactory.createMockTask().cloneBuilder()
                .withOperatorId(operator.getId())
                .build();
        taskDao.create(task);

        // Process
        // 3. delete operator by id
        try {
            operatorService.deleteOperator(operator);
            fail();
        } catch (Exception e) {
            // Validate
            // 4. a RuleOperatorInUseException should be thrown
            assertThat(e, instanceOf(RuleOperatorInUseException.class));
            // 5. message should include specific number of tasks
            assertThat(e.getMessage(), is(
                    String.format("Operator with id \"%d\" is used by at least one task already. Remove these tasks first or replace internal operator of each task to another.", operator.getId())
            ));
        }
    }

    @Test
    public void deleteOperator_withNonExistedId_shouldThrowException() {
        // Prepare
        // 1. create an operator, but not store it
        Operator operator = MockOperatorFactory.createOperator();

        // Process
        // 2. try to delete the operator
        try {
            operatorService.deleteOperator(operator);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }
    }

    @Test
    public void searchOperators() {
        // TODO: implement this
    }
}
