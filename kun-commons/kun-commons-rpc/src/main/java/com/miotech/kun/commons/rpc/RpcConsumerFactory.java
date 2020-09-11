package com.miotech.kun.commons.rpc;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class RpcConsumerFactory {
    private RpcConsumerFactory() {}

    private static final Properties defaultProperties = new Properties();

    private static Optional<RegistryConfig> overrideRegistryConfig = Optional.ofNullable(null);

    // ReferenceConfig object are heavily encapsulated, therefore we cache them for performance
    private static Map<String, ReferenceConfig<?>> cachedReferenceConfig = new HashMap<>();

    /**
     * Directly connects to a service by providing its signature and host URL.
     * Only recommended for debug usage.
     * @param interfaceClass interface type class
     * @param referenceUrl target service host url (example: "dubbo://127.0.0.1:9091")
     * @param <T> interface type
     * @return
     */
    public static <T> T getServiceByDirectConnect(Class<T> interfaceClass, String version, String referenceUrl) {
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(interfaceClass);
        reference.setVersion(version);
        reference.setUrl(referenceUrl);
        return reference.get();
    }

    /**
     * Fetch service stub from global remote registry.
     * @param applicationName Consumer application name, not necessary to be the same name as provider's
     * @param interfaceClass interface type class
     * @param version version of provider service
     * @param <T> interface type
     * @return A service stub. DO remember to store this stub in cache for later reuse.
     */
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
        if (overrideRegistryConfig.isPresent()) {
            return overrideRegistryConfig.get();
        }
        // Else, we use default registry center
        RegistryConfig registryConfig = new RegistryConfig();
        // TODO: allow override default configuration by RpcConfig object
        registryConfig.setAddress(defaultProperties.getProperty("dubbo.registry.address", "redis://127.0.0.1:6379"));
        registryConfig.setUsername(defaultProperties.getProperty("dubbo.registry.username", ""));
        registryConfig.setPassword(defaultProperties.getProperty("dubbo.registry.password", ""));
        return registryConfig;
    }

    public static void useOverrideRegistryConfig(RegistryConfig registryConfig) {
        overrideRegistryConfig = Optional.ofNullable(registryConfig);
    }
}
