package com.miotech.kun.commons.network;

import com.miotech.kun.commons.network.client.RpcResponseCallback;
import com.miotech.kun.commons.network.client.TransportClient;
import com.miotech.kun.commons.network.client.TransportClientFactory;
import com.miotech.kun.commons.network.server.RpcHandler;
import com.miotech.kun.commons.network.server.TransportServer;
import com.miotech.kun.commons.network.utils.JavaUtils;
import com.miotech.kun.commons.network.utils.TransportConf;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RpcIntegrationTest {
    private static TransportConf conf;
    private static TransportContext context;
    private static TransportServer server;
    private static RpcHandler rpcHandler;
    private static TransportClientFactory clientFactory;

    @BeforeClass
    public static void setUp() {
        Properties props = new Properties();
        props.put(TransportConf.NETWORK_IO_SERVERTHREADS_KEY, "1");
        conf = new TransportConf("test", props);
        rpcHandler = new TestRpcHandler();
        context = new TransportContext(conf, rpcHandler, false);
        server = context.createServer();
        clientFactory = context.createClientFactory();
    }

    @Test
    public void test_rpcRequestSuccess() throws Exception {
        RpcResult res = sendRPC("hello/World");
        assertEquals("Hello, World!", res.successMessages);
        assertTrue(res.errorMessages.isEmpty());
    }

    @Test
    public void test_returnErrorRPC() throws Exception {
        RpcResult res = sendRPC("return error/OK");
        assertTrue(res.successMessages.isEmpty());
        assertTrue(res.errorMessages.contains("Returned: OK"));
    }

    @Test
    public void test_throwErrorRPC() throws Exception {
        RpcResult res = sendRPC("throw error/OK");
        assertTrue(res.successMessages.isEmpty());
        assertTrue(res.errorMessages.contains("Thrown: OK"));
    }

    static class RpcResult {
        public String successMessages = "";
        public String errorMessages = "";
    }

    private RpcResult sendRPC(String command) throws Exception {
        TransportClient client = clientFactory.createClient(JavaUtils.getLocalHost(), server.getPort());
        final Semaphore sem = new Semaphore(0);

        final RpcResult res = new RpcResult();

        RpcResponseCallback callback = new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer message) {
                res.successMessages = JavaUtils.bytesToString(message);
                sem.release();
            }

            @Override
            public void onFailure(Throwable e) {
                res.errorMessages = e.getMessage();
                sem.release();
            }
        };

        client.sendRpc(JavaUtils.stringToBytes(command), callback);
        if (!sem.tryAcquire(1, 500, TimeUnit.SECONDS)) {
            fail("Timeout getting response from the server");
        }
        client.close();
        return res;
    }


    private static class TestRpcHandler extends RpcHandler {

        @Override
        public void receive(TransportClient client,
                            ByteBuffer message,
                            RpcResponseCallback callback) {
            String msg = JavaUtils.bytesToString(message);
            String[] parts = msg.split("/");
            switch (parts[0]) {
                case "hello":
                    callback.onSuccess(JavaUtils.stringToBytes("Hello, " + parts[1] + "!"));
                    break;
                case "return error":
                    callback.onFailure(new RuntimeException("Returned: " + parts[1]));
                    break;
                case "throw error":
                    throw new RuntimeException("Thrown: " + parts[1]);
            }
        }
    }
}
