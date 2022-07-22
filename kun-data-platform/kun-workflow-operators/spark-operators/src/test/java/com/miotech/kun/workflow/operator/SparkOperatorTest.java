package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.testing.executor.MockOperatorContextImpl;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;
import static org.junit.Assert.assertTrue;

public class SparkOperatorTest {

    SparkOperator operator = new SparkOperator();
    private MockOperatorContextImpl context;

    @BeforeEach
    public void initSparkOperator(){
        Map<String,Object> params = new HashMap<>();
        params.put(CONF_LIVY_HOST, "http://localhost:8089");
        params.put(CONF_LIVY_PROXY_USER, "hadoop");
        params.put(CONF_LIVY_BATCH_JARS, "s3:bucket/test1.jar");
        params.put(CONF_LIVY_BATCH_FILES, "s3://bucket/main.py,s3://bucket/py.zip");
        params.put(CONF_LIVY_BATCH_APPLICATION, "com.miotect.test.Main");
        params.put(SPARK_APPLICATION_ARGS, " -e dev -s test");
        params.put(CONF_LIVY_BATCH_NAME, "test-spark");
        params.put(CONF_LIVY_BATCH_CONF, "{\"spark.jars\":\"s3:bucket/test2.jar\"}");
        params.put(CONF_LINEAGE_OUTPUT_PATH, "");
        params.put(CONF_LINEAGE_JAR_PATH, "");
        params.put(CONF_S3_ACCESS_KEY, "");
        params.put(CONF_S3_SECRET_KEY, "");
        params.put(SPARK_YARN_HOST, "http://localhost:8088");
        context = new MockOperatorContextImpl(new Config(params), IdGenerator.getInstance().nextId(), ExecuteTarget.newBuilder().build());
        operator.setContext(context);
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
        assertTrue(newConf.getString(SPARK_APPLICATION_ARGS).equals(legacyConf.getString(SPARK_APPLICATION_ARGS)));
        assertTrue(newConf.getString(CONF_LINEAGE_OUTPUT_PATH).equals(legacyConf.getString(CONF_LINEAGE_OUTPUT_PATH)));
        assertTrue(newConf.getString(CONF_LINEAGE_JAR_PATH).equals(legacyConf.getString(CONF_LINEAGE_JAR_PATH)));
        assertTrue(newConf.getString(CONF_S3_ACCESS_KEY).equals(legacyConf.getString(CONF_S3_ACCESS_KEY)));
        assertTrue(newConf.getString(CONF_S3_SECRET_KEY).equals(legacyConf.getString(CONF_S3_SECRET_KEY)));

    }
}