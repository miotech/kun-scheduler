package com.miotech.kun.commons.rpc;

/**
 * Abstraction class for RPC service provider.
 */
public interface KunRemoteServiceProvider {
    /**
     * Bootstrap entry for RPC service provider.
     */
    void bootstrap();

    /**
     * Get service name
     * @return service name string
     */
    String getServiceName();
}
