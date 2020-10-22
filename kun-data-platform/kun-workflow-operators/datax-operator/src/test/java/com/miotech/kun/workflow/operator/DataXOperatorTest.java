package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.testing.executor.MockOperatorContextImpl;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.miotech.kun.workflow.operator.constant.OperatorConfigKey.JOB_JSON;
import static org.junit.Assert.assertTrue;

public class DataXOperatorTest {

    private DataXOperator dataXOperator;

    @Before
    public void setUp() throws IOException {
        dataXOperator = new DataXOperator();

        MockOperatorContextImpl operatorContext = new MockOperatorContextImpl(dataXOperator);
        String jobJson = FileUtils.readFileToString(new File(this.getClass().getClassLoader()
                .getResource("datax-operator-job.json").getPath()), Charset.defaultCharset());
        operatorContext.setParam(JOB_JSON, jobJson);

        dataXOperator.setContext(operatorContext);
    }

    @After
    public void tearDown() {
        dataXOperator = null;
    }

    @Test
    public void testRun() {
        dataXOperator.init();
        assertTrue(dataXOperator.run());
    }

}
