package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.testing.MockServerTestBase;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import org.junit.Before;
import org.junit.Ignore;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SparkOperatorTest extends MockServerTestBase {

    private OperatorRunner operatorRunner;

    @Before
    public void init(){
        KunOperator operator = new SparkOperator();
        operatorRunner = new OperatorRunner(operator);
        operatorRunner.setConfigKey(SparkConfiguration.CONF_LIVY_HOST, getAddress());
    }

    @Ignore
    public void run() {
        Map<String ,String> params = new HashMap<>();
        params.put(SparkConfiguration.CONF_LIVY_BATCH_FILES, "file:///test.jar");
        params.put(SparkConfiguration.CONF_LIVY_BATCH_APPLICATION, "Application");
        params.put(SparkConfiguration.CONF_LIVY_BATCH_ARGS,  "-param1 a -param2 {{b}}");
        params.put(SparkConfiguration.CONF_LIVY_BATCH_NAME,  "testjob");
        params.put(SparkConfiguration.CONF_LIVY_BATCH_CONF,  "{\"key1\":\"value2\"}");
        params.put(SparkConfiguration.CONF_LIVY_DISPATCH_ADDRESS, "hdfs://localhost:8020/tmp/");
        params.put(SparkConfiguration.CONF_LIVY_DISPATCH_CONFIG,  "{\"fs.defaultFS\":\"hdfs://localhost:8020\"}");
        params.put(SparkConfiguration.CONF_VARIABLES,  "{\"b\":\"c\"}");

        operatorRunner.setConfig(params);

        // 1. create session
        String payload = "{\"file\":\"file:///test.jar\",\"proxyUser\":\"hadoop\",\"className\":\"Application\",\"args\":[\"-param1\",\"a\",\"-param2\",\"c\"],\"queue\":\"default\",\"name\":\"testjob\",\"conf\":{\"key1\":\"value2\"}}";
        String response ="{\"id\":0,\"name\":null,\"appId\":\"1\",\"owner\":null,\"proxyUser\":null,\"state\":\"starting\",\"kind\":\"shared\",\"appInfo\":{\"driverLogUrl\":\"http://localhost:8042/node/containerlogs/container_1588732946776_46661_01_000001/livy\",\"sparkUiUrl\":null},\"log\":[\"stdout: \",\"\\nstderr: \",\"\\nYARN Diagnostics: \"]}";
        mockPost("/batches",  payload, response);

        // 2. query session state
        mockGet("/batches/0/state", "{\"state\":\"starting\"}");
        mockGet("/batches/0/state", "{\"state\":\"running\"}");
        mockGet("/batches/0/state", "{\"state\":\"success\"}");

        // 3. refresh batch
        mockGet("/batches/0", response);

        boolean isSuccess = operatorRunner.run();
        assertTrue(isSuccess);
        assertTrue(operatorRunner.getLog().size() > 0);
        assertTrue(String.join("\n", operatorRunner.getLog()).contains("Livy client connect to \"http://localhost:10180\" to queue \"default\" as user \"hadoop\""));
    }

    @Ignore
    public void testRun_abort() {
        Map<String ,String> params = new HashMap<>();
        params.put(SparkConfiguration.CONF_LIVY_BATCH_FILES, "file:///test.jar");
        params.put(SparkConfiguration.CONF_LIVY_BATCH_APPLICATION, "Application");
        params.put(SparkConfiguration.CONF_LIVY_BATCH_ARGS,  "-param1 a -param2 {{b}}");
        params.put(SparkConfiguration.CONF_LIVY_BATCH_NAME,  "testjob");
        params.put(SparkConfiguration.CONF_LIVY_BATCH_CONF,  "{\"key1\":\"value2\"}");
        params.put(SparkConfiguration.CONF_LIVY_DISPATCH_ADDRESS, "hdfs://localhost:8020/tmp/");
        params.put(SparkConfiguration.CONF_LIVY_DISPATCH_CONFIG,  "{\"fs.defaultFS\":\"hdfs://localhost:8020\"}");
        params.put(SparkConfiguration.CONF_VARIABLES,  "{\"b\":\"c\"}");

        operatorRunner.setConfig(params);

        // 1. create session
        String payload = "{\"file\":\"file:///test.jar\",\"proxyUser\":\"hadoop\",\"className\":\"Application\",\"args\":[\"-param1\",\"a\",\"-param2\",\"c\"],\"queue\":\"default\",\"name\":\"testjob\",\"conf\":{\"key1\":\"value2\"}}";
        String response ="{\"id\":0,\"name\":null,\"appId\":\"1\",\"owner\":null,\"proxyUser\":null,\"state\":\"starting\",\"kind\":\"shared\",\"appInfo\":{\"driverLogUrl\":\"http://localhost:8042/node/containerlogs/container_1588732946776_46661_01_000001/livy\",\"sparkUiUrl\":null},\"log\":[\"stdout: \",\"\\nstderr: \",\"\\nYARN Diagnostics: \"]}";
        mockPost("/batches",  payload, response);

        // 2. query session state
        mockGet("/batches/0", response);
        mockGet("/batches/0/state", "{\"state\":\"starting\"}");
        mockGet("/batches/0/state", "{\"state\":\"running\"}");

        // 3. abort during running batch
        try {
            operatorRunner.abortAfter(2, (context) -> {
                mockDelete("/batches/0", "{\"msg\": \"deleted\"}");
                mockGet("/batches/0/state", "{\"msg\": \"Session '0' not found\"}");
            });
            operatorRunner.run();
        } catch (Exception e) {
            assertThat(e.getClass(), is(IllegalStateException.class));
            assertThat("Cannot find state for job: 0", is(e.getMessage()));
        }
    }
}