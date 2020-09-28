package com.miotech.kun.commons.rpc;

import org.apache.dubbo.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcPublishers {
    private static final Logger logger = LoggerFactory.getLogger(RpcPublishers.class);

    private RpcPublishers() { }

    public static <T> T exportService(Class<T> interfaze, String version, T impl) {
        if (!RpcBootstrap.isStarted()) {
            throw new IllegalStateException("Rpc framework is not started yet.");
        }

        ServiceConfig<T> service = new ServiceConfig<>();
        service.setInterface(interfaze);
        service.setVersion(version);
        service.setRef(impl);
        logger.debug("Try to export service. interface={}, version={}", interfaze.getName(), version);
        service.export();
        logger.debug("Export service successfully. interface={}, version={}", interfaze.getName(), version);
        return impl;
    }
}
