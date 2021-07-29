package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.testing.MockServerTestBase;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SparkSubmitOperatorTest {
    private OperatorRunner operatorRunner;

    @Before
    public void init(){
        KunOperator operator = new SparkSubmitOperator();
        operatorRunner = new OperatorRunner(operator);
    }

    @Test
    public void run() {
        operatorRunner.setConfigKey("command", "spark-submit --master local[2] --class org.apache.spark.examples.SparkPi /Users/aijiaguo/git/homebrew/Cellar/apache-spark/2.4.5/libexec/examples/jars/spark-examples_2.11-2.4.5.jar");
        operatorRunner.run();

//        String logs = String.join("\n", operatorRunner.getLog());
//        assertTrue(logs.contains(" hello"));
    }
}
