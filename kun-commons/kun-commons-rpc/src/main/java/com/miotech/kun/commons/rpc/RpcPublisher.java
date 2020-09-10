package com.miotech.kun.commons.rpc;

import com.google.common.base.Strings;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class RpcPublisher {
    public static DubboBootstrap start(RpcConfig config) {
        return DubboBootstrap.getInstance()
                .application(getMergedApplicationConfig(config))
                .registry(getMergedRegistryConfig(config))
                .protocol(getMergedProtocolConfig(config))
                .services(getMergedServiceConfig(config))
                .start();
    }

    private static Properties loadDefaultProperties(String rpcPropertiesConfigFilePath) {
        Properties properties = new Properties();
        InputStream inputStream = RpcPublisher.class.getClassLoader().getResourceAsStream(rpcPropertiesConfigFilePath);
        if (Objects.nonNull(inputStream)) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        } else {
            throw new RuntimeException(String.format("Cannot load RPC properties configuration file: %s", rpcPropertiesConfigFilePath));
        }

        return properties;
    }

    private static ApplicationConfig getMergedApplicationConfig(RpcConfig rpcConfig) {
        ApplicationConfig application = new ApplicationConfig();
        application.setName(rpcConfig.getApplicationName());
        return application;
    }

    private static RegistryConfig getMergedRegistryConfig(RpcConfig config) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(Strings.isNullOrEmpty(config.getRegistryCenterURL()) ? "redis://127.0.0.1:6379" : config.getRegistryCenterURL());
        registryConfig.setUsername(Strings.isNullOrEmpty(config.getRegistryCenterUsername()) ? "" : config.getRegistryCenterUsername());
        registryConfig.setPassword(Strings.isNullOrEmpty(config.getRegistryCenterPassword()) ? "" : config.getRegistryCenterPassword());
        return registryConfig;
    }

    private static ProtocolConfig getMergedProtocolConfig(RpcConfig rpcConfig) {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("dubbo");
        protocolConfig.setPort(rpcConfig.getPort());
        return protocolConfig;
    }

    private static List<ServiceConfig> getMergedServiceConfig(RpcConfig rpcConfig) {
        return rpcConfig.getServices();
    }
}
