package com.miotech.kun.commons.network.rpc;

import com.miotech.kun.commons.network.RpcServer;
import com.miotech.kun.commons.network.utils.TransportConf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class RpcServerTest {

    private static RpcServer rpcServer;

    @Before
    public void initServer() {
        rpcServer = createServer(5001);
        rpcServer.startServer();
    }

    @After
    public void stopServer() {
        if (rpcServer != null) {
            rpcServer.stop();
        }
    }

    @Test
    public void sendAndReceive() {
        String TEST_ENDPOINT = "testSend";
        rpcServer.setUpEndpoint(TEST_ENDPOINT, new RpcMessageEndpoint() {
            @Override
            public Object receiveAndReply(RpcInContext context, Object content) {
                return null;
            }
        });

        RpcServer testOtherServer = createServer(5002);

    }

    private RpcServer createServer(Integer port) {
        Properties props = new Properties();
        props.put(TransportConf.NETWORK_IO_SERVERTHREADS_KEY, "1");
        props.put("rpc.server.host", "localhost");
        props.put("rpc.server.port", port);

        return new RpcServer("test-rpc", props);
    }
}