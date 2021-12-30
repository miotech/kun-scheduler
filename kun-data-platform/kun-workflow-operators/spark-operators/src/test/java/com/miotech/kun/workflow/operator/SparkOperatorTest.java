package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.commons.testing.MockServerTestBase;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.testing.executor.MockOperatorContextImpl;
import com.miotech.kun.workflow.utils.JSONUtils;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;
import static com.miotech.kun.workflow.operator.SparkConfiguration.CONF_S3_SECRET_KEY;
import static org.junit.Assert.assertTrue;

public class SparkOperatorTest {

    SparkOperator operator = new SparkOperator();
    private MockOperatorContextImpl context;

    @BeforeEach
    public void initSparkOperator(){

        context = new MockOperatorContextImpl(operator);
        context.setParam(CONF_LIVY_HOST, "http://localhost:8089");
        context.setParam(CONF_LIVY_PROXY_USER, "hadoop");
        context.setParam(CONF_LIVY_BATCH_JARS, "s3:bucket/test1.jar");
        context.setParam(CONF_LIVY_BATCH_FILES, "s3://bucket/main.py,s3://bucket/py.zip");
        context.setParam(CONF_LIVY_BATCH_APPLICATION, "com.miotect.test.Main");
        context.setParam(CONF_LIVY_BATCH_ARGS, " -e dev -s test");
        context.setParam(CONF_LIVY_BATCH_NAME, "test-spark");
        context.setParam(CONF_LIVY_BATCH_CONF, "{\"spark.jars\":\"s3:bucket/test2.jar\"}");
        context.setParam(CONF_LINEAGE_OUTPUT_PATH, "");
        context.setParam(CONF_LINEAGE_JAR_PATH, "");
        context.setParam(CONF_S3_ACCESS_KEY, "");
        context.setParam(CONF_S3_SECRET_KEY, "");
        context.setParam(SPARK_YARN_HOST, "http://localhost:8088");
    }

    @Test
    public void testTransformConfig(){

        Config legacyConf = context.getConfig();
        Config newConf = operator.transformOperatorConfig(context);

        String sparkParamsStr = newConf.getString(SPARK_SUBMIT_PARMAS);
        Map<String, String> params = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkParamsStr) ? "{}" : sparkParamsStr);
        String sparkConfStr = newConf.getString(SPARK_CONF);
        Map<String, String> sparkConf = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkConfStr) ? "{}" : sparkConfStr);

        assertTrue(params.get(SPARK_ENTRY_CLASS).equals(legacyConf.getString(CONF_LIVY_BATCH_APPLICATION)));
        assertTrue(sparkConf.get("spark.jars").equals("s3:bucket/test2.jar,s3:bucket/test1.jar"));
        assertTrue(sparkConf.get("spark.submit.pyFiles").equals("s3://bucket/py.zip"));

        assertTrue(newConf.getString(SPARK_PROXY_USER).equals(legacyConf.getString(CONF_LIVY_PROXY_USER)));
        assertTrue(newConf.getString(SPARK_APPLICATION).equals("s3://bucket/main.py"));
        assertTrue(newConf.getString(SPARK_APPLICATION_ARGS).equals(legacyConf.getString(CONF_LIVY_BATCH_ARGS)));
        assertTrue(newConf.getString(CONF_LINEAGE_OUTPUT_PATH).equals(legacyConf.getString(CONF_LINEAGE_OUTPUT_PATH)));
        assertTrue(newConf.getString(CONF_LINEAGE_JAR_PATH).equals(legacyConf.getString(CONF_LINEAGE_JAR_PATH)));
        assertTrue(newConf.getString(CONF_S3_ACCESS_KEY).equals(legacyConf.getString(CONF_S3_ACCESS_KEY)));
        assertTrue(newConf.getString(CONF_S3_SECRET_KEY).equals(legacyConf.getString(CONF_S3_SECRET_KEY)));

    }
}