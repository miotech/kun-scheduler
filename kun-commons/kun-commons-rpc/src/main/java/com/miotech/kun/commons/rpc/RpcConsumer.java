package com.miotech.kun.commons.rpc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.dubbo.config.ReferenceConfig;

import java.util.concurrent.ExecutionException;

public class RpcConsumer {
    private final long REFERENCE_CACHE_SIZE = 10L;

    // ReferenceConfig object are heavily encapsulated, therefore we store them as cache for later reuse
    private final Cache<String, ReferenceConfig<?>> referenceConfigCache = CacheBuilder.newBuilder()
            .maximumSize(REFERENCE_CACHE_SIZE)
            .build();

    /**
     * Fetch service stub from global remote registry.
     * @param applicationName Consumer application name, not necessary to be the same name as provider's
     * @param interfaceClass interface type class
     * @param version version of provider service
     * @param <T> interface type
     * @return A service stub. DO remember to store this stub in cache for later reuse.
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(String applicationName, Class<T> interfaceClass, String version) {
        if (!RpcBootstrap.isStarted()) {
            throw new IllegalStateException("Rpc framework is not started yet.");
        }

        // Try load reference from cache
        ReferenceConfig<T> referenceConfig;
        try {
            referenceConfig = (ReferenceConfig<T>) referenceConfigCache.get(applicationName + "::" + interfaceClass.getName(), () -> {
                // If entry is not in cache, create a new one
                return createReferenceConfig(interfaceClass, version);
            });
        } catch (ExecutionException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        return referenceConfig.get();
    }

    private static <T> ReferenceConfig<T> createReferenceConfig(Class<T> interfaceClass, String version) {
        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setVersion(version);
        referenceConfig.setInterface(interfaceClass);
        return referenceConfig;
    }
}
