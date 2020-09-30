package com.miotech.kun.commons.rpc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.dubbo.config.ReferenceConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
    public <T> T getService(String applicationName, Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(
                RpcConsumer.class.getClassLoader(),
                new Class[] { interfaceClass },
                new LazyLoadingServiceProxy<>(applicationName, interfaceClass, version)
        );
    }

    private class LazyLoadingServiceProxy<T> implements InvocationHandler {
        private final String applicationName;
        private final Class<T> interfaceClass;
        private final String version;

        public LazyLoadingServiceProxy(String applicationName, Class<T> interfaceClass, String version) {
            this.applicationName = applicationName;
            this.interfaceClass = interfaceClass;
            this.version = version;
        }

        private T actualService = null;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            synchronized (this) {
                if (actualService == null) {
                    actualService = doGetService(applicationName, interfaceClass, version);
                }
            }
            return method.invoke(actualService, args);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T doGetService(String applicationName, Class<T> interfaceClass, String version) {
        if (!RpcBootstrap.isStarted()) {
            throw new IllegalStateException("Rpc framework is not started yet.");
        }

        ReferenceConfig<T> referenceConfig;
        try {
            referenceConfig = (ReferenceConfig<T>) referenceConfigCache.get(
                    cacheKey(applicationName, interfaceClass),
                    () -> createReferenceConfig(interfaceClass, version));
        } catch (ExecutionException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        return referenceConfig.get();
    }

    private <T> ReferenceConfig<T> createReferenceConfig(Class<T> interfaceClass, String version) {
        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setVersion(version);
        referenceConfig.setInterface(interfaceClass);
        return referenceConfig;
    }

    private String cacheKey(String applicationName, Class<?> clazz) {
       return applicationName + "::" + clazz.getName();
    }
}
