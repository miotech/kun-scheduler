package com.miotech.kun.workflow.operator.model.clients;

import com.miotech.kun.workflow.operator.model.models.SparkApp;
import com.miotech.kun.workflow.operator.model.models.SparkJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LivyClientTest {

    private LivyClient client = new LivyClient("http://<livy_ip>:8998");
    private SparkClient sparkClient = new SparkClient("<livy_ip>", 8088);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void runSparkJob() throws InterruptedException {
        SparkJob job = new SparkJob();
        job.setFile("s3://com.miotech.data.prd/tmp/spark-examples_2.11-2.3.1.jar");
        job.setClassName("org.apache.spark.examples.SparkPi");

        SparkApp app = client.runSparkJob(job);

        Integer jobId = app.getId();
        while(!app.getState().equals("success")) {
            app = client.getSparkJob(jobId);
            assert app.getId() != 0;
            Thread.sleep(3000);
        }
        String applicationState = sparkClient.getApp(app.getAppId())
                .getState();
        assert applicationState.equals("FINISHED");
    }
}