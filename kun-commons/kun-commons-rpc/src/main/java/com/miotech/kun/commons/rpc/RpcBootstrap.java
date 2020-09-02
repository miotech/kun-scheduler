package com.miotech.kun.commons.rpc;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(RpcBootstrap.class);

    private static boolean started = false;

    public static synchronized void start(RpcConfig config) {
        logger.debug("Try to bootstrap rpc framework. application={}, url={}",
                config.getApplicationName(), config.getRegistryCenterURL());
        DubboBootstrap.getInstance()
                .application(getApplicationConfig(config))
                .registry(getRegistryConfig(config))
                .protocol(getProtocol(config))
                .start();
        started = true;
        logger.debug("Rpc framework is started successfully.");
    }

    public static synchronized boolean isStarted() {
        return started;
    }

    private static ApplicationConfig getApplicationConfig(RpcConfig rpcConfig) {
        ApplicationConfig application = new ApplicationConfig();
        application.setName(rpcConfig.getApplicationName());
        return application;
    }

    private static RegistryConfig getRegistryConfig(RpcConfig config) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(config.getRegistryCenterURL());
        registryConfig.setUsername(config.getRegistryCenterUsername());
        registryConfig.setPassword(config.getRegistryCenterPassword());
        return registryConfig;
    }

    private static ProtocolConfig getProtocol(RpcConfig rpcConfig) {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("dubbo");
        protocolConfig.setPort(rpcConfig.getPort());
        return protocolConfig;
    }
}
