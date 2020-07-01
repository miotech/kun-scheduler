package com.miotech.kun.metadata.databuilder.schedule;

import org.junit.Test;

public class DataBuilderOperatorTest {

    @Test(expected = RuntimeException.class)
    public void testRun_lackOfVariable() {
        DataBuilderOperator operator = new DataBuilderOperator();

        MockOperatorContextImpl mockOperatorContext = new MockOperatorContextImpl();
        mockOperatorContext.setVar("env", "dev1");

        operator.setContext(mockOperatorContext);
        operator.init();
        operator.run();
    }

}
