package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.core.execution.Operator;
import org.junit.Test;

import static org.junit.Assert.*;

public class SparkOperatorTest {

    @Test
    public void init() {
    }

    @Test
    public void run() {
        OperatorContextImpl context = new OperatorContextImpl();
        Operator operator = new SparkOperator();
        operator.init(context);
        operator.run(context);
    }

    @Test
    public void onAbort() {
    }
}