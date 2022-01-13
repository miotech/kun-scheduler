package com.miotech.kun.workflow.operator.spark.clients;

import com.miotech.kun.commons.testing.MockServerTestBase;
import com.miotech.kun.workflow.operator.spark.models.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;


public class SparkClientTest extends MockServerTestBase {
    private SparkClient sparkClient;

    @BeforeEach
    public void init(){
        String server = getAddress();
        sparkClient = new SparkClient(server);
    }

    @Test
    public void testGetApp() {
        String appId = "application_1628214162676_39692";
        mockGet("/ws/v1/cluster/apps/application_1628214162676_39692", "{\"app\":{\"id\":\"application_1628214162676_39692\",\"user\":\"hadoop\",\"name\":\"`新闻2.0`: news-operator-seq - 225121685399928832\",\"queue\":\"default\",\"state\":\"FINISHED\",\"finalStatus\":\"SUCCEEDED\",\"progress\":100.0,\"trackingUI\":\"History\",\"trackingUrl\":\"http://ip-10-0-2-14.ap-northeast-1.compute.internal:20888/proxy/application_1628214162676_39692/\",\"diagnostics\":\"\",\"clusterId\":1628214162676,\"applicationType\":\"SPARK\",\"applicationTags\":\"livy-batch-35471-pfypqhdu\",\"priority\":0,\"startedTime\":1630905203970,\"finishedTime\":1630907197965,\"elapsedTime\":1993995,\"amContainerLogs\":\"http://ip-10-0-2-232.ap-northeast-1.compute.internal:8042/node/containerlogs/container_1628214162676_39692_01_000001/hadoop\",\"amHostHttpAddress\":\"ip-10-0-2-232.ap-northeast-1.compute.internal:8042\",\"amRPCAddress\":\"ip-10-0-2-232.ap-northeast-1.compute.internal:32799\",\"allocatedMB\":-1,\"allocatedVCores\":-1,\"runningContainers\":-1,\"memorySeconds\":65920355,\"vcoreSeconds\":21867,\"queueUsagePercentage\":0.0,\"clusterUsagePercentage\":0.0,\"preemptedResourceMB\":0,\"preemptedResourceVCores\":0,\"numNonAMContainerPreempted\":0,\"numAMContainerPreempted\":0,\"preemptedMemorySeconds\":0,\"preemptedVcoreSeconds\":0,\"logAggregationStatus\":\"RUNNING\",\"unmanagedApplication\":false,\"amNodeLabelExpression\":\"CORE\"}}");
        Application app = sparkClient.getApp(appId);
        assertTrue(app.getId().equals(appId));
        assertTrue(app.getFinalStatus().equals("SUCCEEDED"));
        assertTrue(app.getAmContainerLogs().equals("http://ip-10-0-2-232.ap-northeast-1.compute.internal:8042/node/containerlogs/container_1628214162676_39692_01_000001/hadoop"));
    }

    @Test
    public void testKillApplication() {
        String appId = "application_1628214162676_39692";
        mockPut("/ws/v1/cluster/apps/application_1628214162676_39692/state", "{\"state\": \"KILLED\"}", "{\"state\":\"RUNNING\"}");
        sparkClient.killApplication(appId);
    }
}