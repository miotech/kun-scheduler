package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.Operator;

public class TestOperator3 extends Operator {
    public boolean run() {
        throw new IllegalStateException("Failure");
    }
}
