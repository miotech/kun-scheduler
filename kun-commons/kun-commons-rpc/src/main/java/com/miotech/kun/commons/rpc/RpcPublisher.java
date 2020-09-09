package com.miotech.kun.commons.rpc;

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
    private static Properties defaultProperties;

    public static DubboBootstrap start(RpcConfig config) {
        defaultProperties = loadDefaultProperties("rpc-registry-center.properties");
        return DubboBootstrap.getInstance()
                .application(getMergedApplicationConfig(config))
                .registry(getMergedRegistryConfig())
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

    private static RegistryConfig getMergedRegistryConfig() {
        // We use default registry center
        RegistryConfig registryConfig = new RegistryConfig();
        // TODO: allow override default configuration by RpcConfig object
        registryConfig.setAddress(defaultProperties.getProperty("dubbo.registry.address"));
        registryConfig.setUsername(defaultProperties.getProperty("dubbo.registry.username", ""));
        registryConfig.setPassword(defaultProperties.getProperty("dubbo.registry.password", ""));
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
