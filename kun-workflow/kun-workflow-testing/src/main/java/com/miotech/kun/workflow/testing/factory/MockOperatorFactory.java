package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.util.ArrayList;
import java.util.List;

public class MockOperatorFactory {
    public static Operator createOperator() {
        return createOperators(1).get(0);
    }

    public static OperatorPropsVO createOperatorPropsVO() {
        Long id = WorkflowIdGenerator.nextOperatorId();
        return OperatorPropsVO.newBuilder()
                .withName("Operator_" + id)
                .withDescription("Operator" + id + "_description")
                .withParams(new ArrayList<>())
                .withClassName("com.miotech.kun.Operator" + id)
                .withPackagePath("s3://storage.miotech.com/Operator")
                .build();
    }

    public static List<Operator> createOperators(int num) {
        return createOperatorsWithPrefix(num, "Operator", "Operator_description_", "com.miotech.kun.Operator", "s3://storage.miotech.com/Operator");
    }

    public static List<Operator> createOperatorsWithPrefix(int num, String namePrefix, String descriptionPrefix, String classNamePrefix, String packagePathPrefix) {
        List<Operator> operators = new ArrayList<>();

        for (long i = 1; i <= num; i += 1) {
            operators.add(Operator.newBuilder()
                    .withId(i)
                    .withName(namePrefix + "_" + i)
                    .withDescription(descriptionPrefix + num)
                    .withClassName(classNamePrefix + num)
                    .withParams(new ArrayList<>())
                    .withPackagePath(packagePathPrefix + num + ".jar")
                    .build()
            );
        }

        return operators;
    }
}
