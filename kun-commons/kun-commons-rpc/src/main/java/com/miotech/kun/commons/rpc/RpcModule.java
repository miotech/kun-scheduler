package com.miotech.kun.commons.rpc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

public class RpcModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(RpcModule.class);

    private final Props props;

    public RpcModule(Props props) {
        this.props = props;
        init();
    }

    private void init() {
        if (!RpcBootstrap.isStarted()) {
            logger.info("Rpc framework is not started, try to bootstrap.");

            String appName = props.getString("rpc.app", "default");
            String registry = props.getString("rpc.registry");
            int port = props.getInt("rpc.port");

            RpcConfig config = new RpcConfig(appName, registry);
            config.setPort(port);
            logger.info("Try to start rpc framework. appName={}, registry={}, port={}", appName, registry, port);

            RpcBootstrap.start(config);
        } else {
            logger.warn("Rpc framework is already started. Skip.");
        }
    }

    @Singleton
    @Provides
    public RpcConsumer rpcConsumer() {
        checkState(RpcBootstrap.isStarted(), "rpc framework should be bootstrapped.");
        return new RpcConsumer();
    }

    @Singleton
    @Provides
    public RpcPublisher rpcPublisher() {
        checkState(RpcBootstrap.isStarted(), "rpc framework should be bootstrapped.");
        return new RpcPublisher();
    }
}
