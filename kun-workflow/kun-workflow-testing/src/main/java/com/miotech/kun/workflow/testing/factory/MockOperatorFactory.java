package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.testing.operator.NopOperator;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.util.ArrayList;
import java.util.List;

public class MockOperatorFactory {
    private static final String PACKAGE_PATH_NOP_OPERATOR = OperatorCompiler.compileJar(NopOperator.class, "NopOperator");

    public static Operator createOperator() {
        return createOperators(1).get(0);
    }

    public static OperatorPropsVO createOperatorPropsVO() {
        Long id = WorkflowIdGenerator.nextOperatorId();
        return OperatorPropsVO.newBuilder()
                .withName("Operator_" + id)
                .withDescription("Operator" + id + "_description")
                .withClassName("com.miotech.kun.Operator" + id)
                .build();
    }

    public static List<Operator> createOperators(int num) {
        return createOperatorsWithPrefix(num, "Operator", "Operator_description_", "NopOperator", PACKAGE_PATH_NOP_OPERATOR);
    }

    public static List<Operator> createOperatorsWithPrefix(int num, String namePrefix, String descriptionPrefix, String className, String packagePath) {
        List<Operator> operators = new ArrayList<>();

        for (long i = 1; i <= num; i += 1) {
            Long id = WorkflowIdGenerator.nextOperatorId();
            operators.add(Operator.newBuilder()
                    .withId(id)
                    .withName(namePrefix + "_" + i)
                    .withDescription(descriptionPrefix + num)
                    .withClassName(className)
                    .withPackagePath(packagePath)
                    .build()
            );
        }

        return operators;
    }
}
