package com.miotech.kun.commons.rpc;

import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class RpcConsumerFactory {
    private RpcConsumerFactory() {}

    private static final Properties defaultProperties = new Properties(); // = loadDefaultProperties("rpc-registry-center.properties");

    // ReferenceConfig object are heavily encapsulated, therefore we cache them for performance
    private static Map<String, ReferenceConfig<?>> cachedReferenceConfig = new HashMap<>();

    public static <T> T getService(String applicationName, Class<T> interfaceClass, String version) {
        ReferenceConfig<T> referenceConfig;
        if (cachedReferenceConfig.containsKey(applicationName + "::" + interfaceClass.getName())) {
            referenceConfig = (ReferenceConfig<T>) cachedReferenceConfig.get(applicationName + "::" + interfaceClass.getName());
        } else {
            referenceConfig = createReferenceConfig(applicationName, interfaceClass, version);
            cachedReferenceConfig.put(applicationName + "::" + interfaceClass.getName(), referenceConfig);
        }

        return referenceConfig.get();
    }

    private static <T> ReferenceConfig<T> createReferenceConfig(String applicationName, Class<T> interfaceClass, String version) {
        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        ApplicationConfig application = new ApplicationConfig();
        application.setName(applicationName);

        referenceConfig.setApplication(application);
        referenceConfig.setRegistry(getMergedRegistryConfig());
        referenceConfig.setVersion(version);
        referenceConfig.setInterface(interfaceClass);

        return referenceConfig;
    }

    private static RegistryConfig getMergedRegistryConfig() {
        // We use default registry center
        RegistryConfig registryConfig = new RegistryConfig();
        // TODO: allow override default configuration by RpcConfig object
        registryConfig.setAddress(defaultProperties.getProperty("dubbo.registry.address", "redis://127.0.0.1:6379"));
        registryConfig.setUsername(defaultProperties.getProperty("dubbo.registry.username", ""));
        registryConfig.setPassword(defaultProperties.getProperty("dubbo.registry.password", ""));
        return registryConfig;
    }

    private static Properties loadDefaultProperties(String rpcPropertiesConfigFilePath) {
        Properties properties = new Properties();
        InputStream inputStream = RpcConsumerFactory.class.getClassLoader().getResourceAsStream(rpcPropertiesConfigFilePath);
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
}
