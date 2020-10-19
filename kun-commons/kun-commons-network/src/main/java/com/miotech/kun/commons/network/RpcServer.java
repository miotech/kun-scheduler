package com.miotech.kun.commons.network;

import com.miotech.kun.commons.network.TransportContext;
import com.miotech.kun.commons.network.rpc.Dispatcher;
import com.miotech.kun.commons.network.rpc.KunRpcHandler;
import com.miotech.kun.commons.network.rpc.RpcMessageEndpoint;
import com.miotech.kun.commons.network.serialize.JavaSerialization;
import com.miotech.kun.commons.network.serialize.Serialization;
import com.miotech.kun.commons.network.server.TransportServer;
import com.miotech.kun.commons.network.utils.TransportConf;

import java.util.Properties;

public class RpcServer {

    private final TransportContext transportContext;
    private final Dispatcher dispatcher;
    private TransportServer transportServer;

    private Properties props;

    public RpcServer(String name, Properties props) {
        this.props = props;
        TransportConf transportConf = new TransportConf(name, props);
        // TODO: initialize serialization from config
        Serialization serialization = new JavaSerialization();
        dispatcher = new Dispatcher(name + "-dispatcher", serialization);
        this.transportContext = new TransportContext(transportConf, new KunRpcHandler(dispatcher));
    }

    public void startServer() {
        dispatcher.start();
        String hostAddress = props.getProperty("rpc.server.host");
        String port = props.getProperty("rpc.server.port", "5000");
        transportServer = transportContext.createServer(hostAddress, Integer.parseInt(port));
    }

    protected void setUpEndpoint(String name, RpcMessageEndpoint endpoint) {
        dispatcher.registerEndpoint(name, endpoint);
    }

    public void stop() {
        dispatcher.stop();
        transportServer.close();
    }
}
