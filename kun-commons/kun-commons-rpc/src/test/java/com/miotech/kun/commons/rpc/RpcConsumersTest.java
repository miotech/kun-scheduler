package com.miotech.kun.commons.rpc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class RpcConsumersTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcConsumersTest.class);

    private static class RpcTestInterfaceImpl implements RpcTestInterface {
        @Override
        public String ping(String msg) {
            return "Pong: " + msg;
        }
    }

    private static class RpcTestInterface2Impl implements RpcTestInterface2 {
        @Override
        public String ping2(String msg) {
            return "Pong2: " + msg;
        }
    }

    private static final RpcTestInterface impl = new RpcTestInterfaceImpl();

    private static final RpcTestInterface2 impl2 = new RpcTestInterface2Impl();

    @BeforeClass
    public static void beforeClass() throws Exception {
        RpcConfig config = new RpcConfig("TestRpcApplication", "multicast://224.5.6.7:1234");
        config.setPort(9001);
        RpcBootstrap.start(config);
    }

    @Test
    public void getService_withRegisteredRPCServiceProvider_shouldWorkProperly() {
        // Prepare: setup provider service
        RpcPublishers.exportService(RpcTestInterface.class, "1.0", impl);

        // Execute: get consumer service stub
        logger.debug("Fetching service provider stub...");
        RpcTestInterface serviceStub = RpcConsumers.getService("TestServiceProvider", RpcTestInterface.class, "1.0");

        // Validate
        String response = serviceStub.ping("Hello");
        assertEquals("Pong: Hello", response);
    }

    @Test
    public void getService_withoutExistingRPCServiceProvider_shouldFailed() {
        // No provider initialized
        try {
            RpcConsumers.getService("TestService2Provider", RpcTestInterface2.class, "2.0");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalStateException.class));
        }
    }
}
