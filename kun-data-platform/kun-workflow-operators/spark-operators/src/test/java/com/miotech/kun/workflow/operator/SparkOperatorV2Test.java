package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.testing.executor.MockOperatorContextImpl;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

public class SparkOperatorV2Test {
    SparkOperatorV2 operator = new SparkOperatorV2();
    private MockOperatorContextImpl context;
    SparkOperatorUtils sparkOperatorUtils;
    private OperatorRunner runner;

    @BeforeEach
    public void initSparkOperator() {
        Map<String, Object> params = new HashMap<>();
        params.put(SPARK_SUBMIT_PARMAS, "{\"class\":\"com.miotech.sql.Application\"}");
        params.put(SPARK_CONF, "");
        params.put(SPARK_PROXY_USER, "hadoop");
        params.put(SPARK_APPLICATION, "s3://bucket/sql.jar");
        params.put(SPARK_APPLICATION_ARGS, " -e dev ");
        params.put(SPARK_YARN_HOST, "http://localhost:8088");
        params.put(CONF_LINEAGE_OUTPUT_PATH, "");
        params.put(CONF_LINEAGE_JAR_PATH, "");
        params.put(CONF_S3_ACCESS_KEY, "");
        params.put(CONF_S3_SECRET_KEY, "");
        runner = new OperatorRunner(operator);
        runner.setConfig(params);

    }

    @Test
    public void testExecCmdWithoutLineage() {
        // prepare
        sparkOperatorUtils = Mockito.spy(SparkOperatorUtils.class);
        Reflect.on(operator).set("sparkOperatorUtils", sparkOperatorUtils);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        doReturn(true).when(sparkOperatorUtils).execSparkSubmitCmd(captor.capture());

        // process
        runner.run();

        // verify
        List<String> cmd = captor.getValue();
        String cmdString = String.join(" ", cmd);
        assertTrue(cmd.get(0).equals("spark-submit"));
        assertFalse(cmdString.contains("spark.fs.s3a.access.key"));
        assertFalse(cmdString.contains("spark.sql.queryExecutionListeners"));
        assertTrue(cmdString.endsWith("-e dev"));
        assertTrue(cmdString.contains("spark.hadoop.taskRun.scheduledTick=000000000000"));
    }

    @Test
    public void testCmdWithLineage() {

        // prepare
        Map<String, Object> params = new HashMap<>();
        params.put(CONF_LINEAGE_OUTPUT_PATH, "s3://bucket/lineage/output");
        params.put(CONF_LINEAGE_JAR_PATH, "s3://bucket/lineage.jar");
        params.put(CONF_S3_ACCESS_KEY, "s3_access_key");
        params.put(CONF_S3_SECRET_KEY, "s3_secret_key");
        params.put(SPARK_CONF, "{\"spark.dynamicAllocation.maxExecutors\":\"10\"}");

        sparkOperatorUtils = Mockito.spy(SparkOperatorUtils.class);
        Reflect.on(operator).set("sparkOperatorUtils", sparkOperatorUtils);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        doReturn(true).when(sparkOperatorUtils).execSparkSubmitCmd(captor.capture());

        // process
        runner.setConfig(params);
        runner.run();

        // verify
        List<String> cmd = captor.getValue();
        String cmdString = String.join(" ", cmd);
        assertTrue(cmd.contains("spark.sql.queryExecutionListeners=za.co.absa.spline.harvester.listener.SplineQueryExecutionListener"));
        assertTrue(cmd.contains("spark.hadoop.spline.hdfs_dispatcher.address=s3://bucket/lineage/output"));
        assertTrue(cmd.contains("spark.fs.s3a.access.key=s3_access_key"));
        assertTrue(cmd.contains("spark.dynamicAllocation.maxExecutors=10"));
        assertTrue(cmdString.contains("s3://bucket/lineage.jar"));
        assertTrue(cmdString.contains("spark.hadoop.taskRun.scheduledTick=000000000000"));
    }
}