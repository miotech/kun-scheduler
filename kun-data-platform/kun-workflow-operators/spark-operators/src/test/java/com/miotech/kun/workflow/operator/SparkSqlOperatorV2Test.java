package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.testing.executor.MockOperatorContextImpl;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class SparkSqlOperatorV2Test {
    SparkSqlOperatorV2 operator = new SparkSqlOperatorV2();
    private MockOperatorContextImpl context;
    Map<String, String> sparkSubmitParams;
    Map<String, String> sparkConf;
    private OperatorRunner runner;

    @BeforeEach
    public void initSparkSqlOperator() {

        Map<String,Object> params = new HashMap<>();
        params.put(SPARK_SUBMIT_PARMAS, "{\"class\":\"com.miotech.sql.Application\"}");
        params.put(SPARK_CONF, "");
        params.put(SPARK_PROXY_USER, "hadoop");
        params.put(SPARK_APPLICATION, "s3://bucket/sql.jar");
        params.put(SPARK_APPLICATION_ARGS, " select * from ${ref('db.table')}_${execute_time} ");
        params.put(SPARK_YARN_HOST, "http://localhost:8088");
        params.put(CONF_LINEAGE_OUTPUT_PATH, "");
        params.put(CONF_LINEAGE_JAR_PATH, "");
        params.put(CONF_S3_ACCESS_KEY, "");
        params.put(CONF_S3_SECRET_KEY, "");
        runner = new OperatorRunner(operator);
        runner.setConfig(params);
        context = runner.getContext();
        Config config = context.getConfig();
        String sparkParamsStr = config.getString(SPARK_SUBMIT_PARMAS);
        sparkSubmitParams = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkParamsStr) ? "{}" : sparkParamsStr);
        String sparkConfStr = config.getString(SPARK_CONF);
        sparkConf = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkConfStr) ? "{}" : sparkConfStr);
    }

    @Test
    public void testRunTimeParams(){
        operator.addRunTimeParams(sparkSubmitParams, context, new HashMap<>());
        assertTrue(sparkSubmitParams.get(SPARK_ENTRY_CLASS).equals("com.miotech.sql.Application"));
        assertTrue(sparkSubmitParams.get(SPARK_PROXY_USER).equals("hadoop"));
        assertTrue(sparkSubmitParams.get(SPARK_DEPLOY_MODE).equals("cluster"));
        assertTrue(sparkSubmitParams.get(SPARK_MASTER).equals("yarn"));

    }

    @Test
    public void testSparkConf(){
        operator.addRunTimeSparkConfs(sparkConf, context);

        assertTrue(sparkConf.get("spark.hadoop.taskRun.scheduledTick").equals("000000000000"));
        assertFalse(sparkConf.containsKey("spark.hadoop.spline.hdfs_dispatcher.address"));
        assertFalse(sparkConf.containsKey("spark.sql.queryExecutionListeners"));
    }

    @Test
    public void testSparkConfWithLineage(){
        Map<String,Object> params = new HashMap<>();
        params.put(CONF_LINEAGE_OUTPUT_PATH, "s3://bucket/lineage/output");
        params.put(CONF_LINEAGE_JAR_PATH, "s3://bucket/lineage.jar");
        params.put(CONF_S3_ACCESS_KEY, "s3_access_key");
        params.put(CONF_S3_SECRET_KEY, "s3_secret_key");
        context.overwriteConfig(new Config(params));

        Config config = context.getConfig();
        String sparkConfStr = config.getString(SPARK_CONF);
        Map<String, String> sparkConf = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkConfStr) ? "{}" : sparkConfStr);
        operator.addRunTimeSparkConfs(sparkConf, context);

        assertTrue(sparkConf.get("spark.fs.s3a.access.key").equals("s3_access_key"));
        assertTrue(sparkConf.get("spark.fs.s3a.secret.key").equals("s3_secret_key"));
        assertTrue(sparkConf.get("spark.hadoop.spline.hdfs_dispatcher.address").equals("s3://bucket/lineage/output"));
        assertTrue(sparkConf.get("spark.sql.queryExecutionListeners").equals("za.co.absa.spline.harvester.listener.SplineQueryExecutionListener"));
        assertTrue(sparkConf.get("spark.jars").contains("s3://bucket/lineage.jar"));
    }

    @Test
    public void testBuildCmd() {
        Config config = context.getConfig();

        // add run time configs
        operator.addRunTimeSparkConfs(sparkConf, context);
        operator.addRunTimeParams(sparkSubmitParams, context, sparkConf);
        List<String> cmd = operator.buildCmd(sparkSubmitParams, sparkConf, config.getString(SPARK_APPLICATION), config.getString(SPARK_APPLICATION_ARGS));
        int len = cmd.size();
        assertTrue (cmd.get(len-1).endsWith(".sql"));

        String tmpSqlFile = sparkConf.get("spark.files");
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(tmpSqlFile), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        assertTrue(contentBuilder.toString().trim().equals("select * from db_test.table_000000000000"));
        assertTrue (cmd.get(len-2).endsWith("-f"));
        assertTrue (cmd.get(len-3).endsWith("s3://bucket/sql.jar"));
    }

    @Test
    public void testExecCmd(){
        // prepare
        SparkOperatorUtils sparkOperatorUtils = Mockito.spy(SparkOperatorUtils.class);
        SparkSqlOperatorV2 mockOperator = new SparkSqlOperatorV2();
        Reflect.on(mockOperator).set("sparkOperatorUtils", sparkOperatorUtils);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        doReturn(true).when(sparkOperatorUtils).execSparkSubmitCmd(anyList());
        // process
        OperatorRunner operatorRunner = new OperatorRunner(mockOperator);
        operatorRunner.setConfig(context.getConfig().getValues());
        operatorRunner.run();
        verify(sparkOperatorUtils).execSparkSubmitCmd(captor.capture());


        // verify
        List<String> cmd  = captor.getValue();
        assertTrue(cmd.get(0).equals("spark-submit"));
        assertTrue(cmd.get(cmd.size()-1).endsWith(".sql"));
    }

}