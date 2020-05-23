package com.miotech.kun.common.test.factory;

import com.miotech.kun.workflow.core.model.operator.Operator;

import java.util.ArrayList;
import java.util.List;

public class MockOperatorFactory {
    public static Operator createOperator() {
        return createOperators(1).get(0);
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
