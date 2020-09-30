package com.miotech.kun.commons.rpc;

import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.utils.Props;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class RpcConsumerTest extends GuiceTestBase {
    private static final Logger logger = LoggerFactory.getLogger(RpcConsumerTest.class);

    @Override
    protected void configuration() {
        Props props = new Props();
        props.put("rpc.registry", "multicast://224.5.6.7:1234");
        props.put("rpc.port", 9001);
        addModules(new RpcModule(props));
    }

    @Inject
    private RpcConsumer rpcConsumer;

    @Inject
    private RpcPublisher rpcPublisher;

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

    @Test
    public void getService_withRegisteredRPCServiceProvider_shouldWorkProperly() {
        // Prepare: setup provider service
        rpcPublisher.exportService(RpcTestInterface.class, "1.0", impl);

        // Execute: get consumer service stub
        logger.debug("Fetching service provider stub...");
        RpcTestInterface serviceStub = rpcConsumer.getService("TestServiceProvider", RpcTestInterface.class, "1.0");

        // Validate
        String response = serviceStub.ping("Hello");
        assertEquals("Pong: Hello", response);
    }

    @Test
    public void getService_withoutExistingRPCServiceProvider_shouldFailed() {
        // No provider initialized
        try {
            rpcConsumer.getService("TestService2Provider", RpcTestInterface2.class, "2.0");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalStateException.class));
        }
    }
}
