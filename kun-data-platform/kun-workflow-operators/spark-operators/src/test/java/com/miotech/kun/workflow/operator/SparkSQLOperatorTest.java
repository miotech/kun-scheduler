package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.testing.executor.MockOperatorContextImpl;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;
import static org.junit.Assert.assertTrue;


public class SparkSQLOperatorTest {
    SparkSQLOperator operator = new SparkSQLOperator();
    private MockOperatorContextImpl context;
    private OperatorRunner runner;

    @BeforeEach
    public void initSparkSQLOperator() {

        Map<String,Object> params = new HashMap<>();
        params.put(CONF_LIVY_HOST, "http://localhost:8089");
        params.put(CONF_LIVY_PROXY_USER, "hadoop");
        params.put(CONF_LIVY_BATCH_JARS, "s3:bucket/test1.jar");
        params.put(CONF_LIVY_BATCH_FILES, "s3://bucket/main.py,s3://bucket/etl.jar");
        params.put(CONF_LIVY_SHARED_SESSION_NAME, "test-sql");
        params.put(CONF_SPARK_SQL, " select 1 ");
        params.put(CONF_LIVY_BATCH_CONF, "{\"spark.jars\":\"s3:bucket/test2.jar\"}");
        params.put(CONF_LINEAGE_OUTPUT_PATH, "");
        params.put(CONF_LINEAGE_JAR_PATH, "");
        params.put(CONF_S3_ACCESS_KEY, "");
        params.put(CONF_S3_SECRET_KEY, "");
        params.put(SPARK_YARN_HOST, "http://localhost:8088");
        params.put(SPARK_APPLICATION, "s3://bucket/sql.jar");
        runner = new OperatorRunner(operator);
        runner.setConfig(params);
        context = runner.getContext();

    }

    @Test
    public void testTransformConfig() {

        Config legacyConf = context.getConfig();
        Config newConf = operator.transformOperatorConfig(context);

        String sparkParamsStr = newConf.getString(SPARK_SUBMIT_PARMAS);
        Map<String, String> params = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkParamsStr) ? "{}" : sparkParamsStr);
        String sparkConfStr = newConf.getString(SPARK_CONF);
        Map<String, String> sparkConf = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkConfStr) ? "{}" : sparkConfStr);

        assertTrue(params.get(SPARK_ENTRY_CLASS).equals("com.miotech.kun.sql.Application"));
        assertTrue(sparkConf.get("spark.jars").equals("s3:bucket/test2.jar,s3:bucket/test1.jar"));


        assertTrue(newConf.getString(SPARK_PROXY_USER).equals(legacyConf.getString(CONF_LIVY_PROXY_USER)));
        assertTrue(newConf.getString(SPARK_APPLICATION).equals(legacyConf.getString(SPARK_APPLICATION)));
        assertTrue(newConf.getString(SPARK_APPLICATION_ARGS).equals(legacyConf.getString(CONF_SPARK_SQL)));
        assertTrue(newConf.getString(CONF_LINEAGE_OUTPUT_PATH).equals(legacyConf.getString(CONF_LINEAGE_OUTPUT_PATH)));
        assertTrue(newConf.getString(CONF_LINEAGE_JAR_PATH).equals(legacyConf.getString(CONF_LINEAGE_JAR_PATH)));
        assertTrue(newConf.getString(CONF_S3_ACCESS_KEY).equals(legacyConf.getString(CONF_S3_ACCESS_KEY)));
        assertTrue(newConf.getString(CONF_S3_SECRET_KEY).equals(legacyConf.getString(CONF_S3_SECRET_KEY)));
    }
}