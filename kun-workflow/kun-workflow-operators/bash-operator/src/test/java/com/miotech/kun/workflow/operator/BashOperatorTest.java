package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BashOperatorTest {
    private OperatorRunner operatorRunner;

    @Before
    public void init(){
        KunOperator operator = new BashOperator();
        operatorRunner = new OperatorRunner(operator);
    }

    @Test
    public void run() {
        operatorRunner.setConfigKey("command", "echo hello");

        operatorRunner.run();

        String logs = String.join("\n", operatorRunner.getLog());
        assertTrue(logs.contains(" hello"));
    }

    @Test
    public void run_and_abort() {
        operatorRunner.setConfigKey("command", "sleep 200");

        operatorRunner.abortAfter(4, null);
        operatorRunner.run();

        String logs = String.join("\n", operatorRunner.getLog());
        assertTrue(logs.contains("Process is successfully terminated"));
    }
}