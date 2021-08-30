package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.testing.MockServerTestBase;
import com.miotech.kun.workflow.testing.executor.MockOperatorContextImpl;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import org.joor.Reflect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;
import static com.miotech.kun.workflow.operator.SparkConfiguration.CONF_S3_SECRET_KEY;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;

public class SparkOperatorV2Test {
    SparkOperatorV2 operator = new SparkOperatorV2();
    private MockOperatorContextImpl context;
    SparkOperatorUtils sparkOperatorUtils;

    @Before
    public void initSparkOperator() {
        context = new MockOperatorContextImpl(operator);
        context.setParam(SPARK_SUBMIT_PARMAS, "{\"class\":\"com.miotech.sql.Application\"}");
        context.setParam(SPARK_CONF, "");
        context.setParam(SPARK_PROXY_USER, "hadoop");
        context.setParam(SPARK_APPLICATION, "s3://bucket/sql.jar");
        context.setParam(SPARK_APPLICATION_ARGS, " -e dev ");
        context.setParam(SPARK_YARN_HOST, "http://localhost:8088");
        context.setParam(CONF_LINEAGE_OUTPUT_PATH, "");
        context.setParam(CONF_LINEAGE_JAR_PATH, "");
        context.setParam(CONF_S3_ACCESS_KEY, "");
        context.setParam(CONF_S3_SECRET_KEY, "");

    }

    @Test
    public void testExecCmdWithoutLineage() {
        // prepare
        sparkOperatorUtils = Mockito.spy(SparkOperatorUtils.class);
        Reflect.on(operator).set("sparkOperatorUtils", sparkOperatorUtils);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        doReturn(true).when(sparkOperatorUtils).execSparkSubmitCmd(captor.capture());

        // process
        OperatorRunner runner = new OperatorRunner(operator);
        runner.setContext(context);
        runner.run();

        // verify
        List<String> cmd = captor.getValue();
        String cmdString = String.join(" ", cmd);
        assertTrue(cmd.get(0).equals("spark-submit"));
        assertFalse(cmdString.contains("spark.fs.s3a.access.key"));
        assertFalse(cmdString.contains("spark.sql.queryExecutionListeners"));
        assertTrue(cmdString.endsWith("-e dev"));
    }

    @Test
    public void testCmdWithLineage() {

        // prepare
        context.setParam(CONF_LINEAGE_OUTPUT_PATH, "s3://bucket/lineage/output");
        context.setParam(CONF_LINEAGE_JAR_PATH, "s3://bucket/lineage.jar");
        context.setParam(CONF_S3_ACCESS_KEY, "s3_access_key");
        context.setParam(CONF_S3_SECRET_KEY, "s3_secret_key");
        context.setParam(SPARK_CONF, "{\"spark.dynamicAllocation.maxExecutors\":\"10\"}");

        sparkOperatorUtils = Mockito.spy(SparkOperatorUtils.class);
        Reflect.on(operator).set("sparkOperatorUtils", sparkOperatorUtils);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        doReturn(true).when(sparkOperatorUtils).execSparkSubmitCmd(captor.capture());

        // process
        OperatorRunner runner = new OperatorRunner(operator);
        runner.setContext(context);
        runner.run();

        // verify
        List<String> cmd = captor.getValue();
        String cmdString = String.join(" ", cmd);
        assertTrue(cmd.contains("spark.sql.queryExecutionListeners=za.co.absa.spline.harvester.listener.SplineQueryExecutionListener"));
        assertTrue(cmd.contains("spark.hadoop.spline.hdfs_dispatcher.address=s3://bucket/lineage/output"));
        assertTrue(cmd.contains("spark.fs.s3a.access.key=s3_access_key"));
        assertTrue(cmd.contains("spark.dynamicAllocation.maxExecutors=10"));
        assertTrue(cmdString.contains("s3://bucket/lineage.jar"));
    }
}