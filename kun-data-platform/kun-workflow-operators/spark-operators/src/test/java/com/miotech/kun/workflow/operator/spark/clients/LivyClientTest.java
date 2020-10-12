package com.miotech.kun.workflow.operator.spark.clients;

import com.miotech.kun.workflow.operator.spark.models.SparkApp;
import com.miotech.kun.workflow.operator.spark.models.SparkJob;
import org.awaitility.Awaitility;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

@Ignore
public class LivyClientTest {

    private LivyClient client = new LivyClient("http://<livy_ip>:8998");
    private SparkClient sparkClient = new SparkClient("<livy_ip>", 8088);

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
            Awaitility.await().atMost(3, TimeUnit.SECONDS);
        }
        String applicationState = sparkClient
                .getApp(app.getAppId())
                .getState();
        assert applicationState.equals("FINISHED");
    }
}