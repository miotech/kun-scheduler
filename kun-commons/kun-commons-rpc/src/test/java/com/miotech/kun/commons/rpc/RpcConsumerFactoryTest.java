package com.miotech.kun.commons.rpc;

import com.miotech.kun.commons.rpctest.SimpleRegistryService;
import org.apache.dubbo.config.RegistryConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class RpcConsumerFactoryTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcConsumerFactoryTest.class);

    private static final AtomicReference<Thread> simpleRegistryServiceRunnerThread = new AtomicReference<>(null);

    private static final AtomicReference<String> simpleRegistryUrl = new AtomicReference<>();

    private static final int TEST_EXECUTION_TIME_LIMIT_SECONDS = 30;

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
    public static void setup() {
        SimpleRegistryService simpleRegistryService = new SimpleRegistryService(9090);

        RegistryConfig testRegistryConfig = new RegistryConfig();
        testRegistryConfig.setAddress(simpleRegistryService.getRegistryURL());
        RpcConsumerFactory.useOverrideRegistryConfig(testRegistryConfig);

        // setup and run simple registry service runner for testing
        Thread providerAndRegistryThread = new Thread(() -> {
            logger.info("Booting up simple registry service runner for testing...");
            try {
                simpleRegistryService.start();
                // This thread will automatically halt after 30 seconds
                // If spent more than this time limitation, cases should failed.
                Thread.sleep(TEST_EXECUTION_TIME_LIMIT_SECONDS * 1000);
                fail();
            } catch (InterruptedException e) {
                simpleRegistryService.disconnect();
                Thread.currentThread().stop();
            }
        });
        logger.info("Configuring runner thread...");
        simpleRegistryServiceRunnerThread.set(providerAndRegistryThread);
        simpleRegistryUrl.set(simpleRegistryService.getRegistryURL());
        providerAndRegistryThread.start();
    }

    @AfterClass
    public static void tearDown() {
        // shutdown registry service, if exists.
        if (Objects.nonNull(simpleRegistryServiceRunnerThread.get())) {
            simpleRegistryServiceRunnerThread.get().interrupt();
        }
    }

    @Test
    public void getService_withRegisteredRPCServiceProvider_shouldWorkProperly() {
        // Prepare: setup provider service
        RpcConfig serviceProviderConfig = new RpcConfig("TestServiceProvider", simpleRegistryUrl.get());
        serviceProviderConfig.setPort(9091);
        serviceProviderConfig.addService(RpcTestInterface.class, "1.0", impl);
        RpcPublisher.exportServices(serviceProviderConfig);

        // Execute: get consumer service stub
        logger.debug("Fetching service provider stub...");
        RpcTestInterface serviceStub = RpcConsumerFactory.getService("TestServiceProvider", RpcTestInterface.class, "1.0");

        // Validate
        String response = serviceStub.ping("Hello");
        assertEquals("Pong: Hello", response);
    }

    @Test
    public void getService_withoutExistingRPCServiceProvider_shouldFailed() {
        // No provider initialized
        try {
            RpcConsumerFactory.getService("TestService2Provider", RpcTestInterface2.class, "2.0");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalStateException.class));
        }
    }

    @Test
    public void getServiceByDirectConnect_withoutRegistrySpecified_shouldWork() {
        // Prepare: setup provider service
        RpcConfig serviceProviderConfig = new RpcConfig("TestService3Provider", simpleRegistryUrl.get());
        serviceProviderConfig.setPort(9092);
        serviceProviderConfig.addService(RpcTestInterface2.class, "1.1", impl2);
        RpcPublisher.exportServices(serviceProviderConfig);

        // Execute: get consumer service stub
        logger.debug("Fetching service provider stub...");
        RpcTestInterface2 serviceStub = RpcConsumerFactory.getServiceByDirectConnect(RpcTestInterface2.class, "1.1", "dubbo://localhost:9092");

        // Validate
        String response = serviceStub.ping2("Hi");
        assertEquals("Pong2: Hi", response);
    }
}
