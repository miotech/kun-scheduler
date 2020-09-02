package com.miotech.kun.metadata.web.rpc;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.miotech.kun.commons.rpc.RpcBootstrap;
import com.miotech.kun.commons.rpc.RpcConfig;
import com.miotech.kun.commons.rpc.RpcPublishers;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataRPCServer {
    private final Logger logger = LoggerFactory.getLogger(MetadataRPCServer.class);

    private final String APPLICATION_NAME = "METADATA_RPC";

    @Inject
    private MetadataServiceFacadeImpl metadataServiceFacadeImpl;

    private RpcConfig getMergedConfiguration(Props props) {
        RpcConfig config = new RpcConfig(APPLICATION_NAME);
        if (props.containsKey("metadata.rpc.registry")) {
            config.setRegistry(props.getString("metadata.rpc.registry"));
        }
        config.setPort(props.getInt("metadata.rpc.port"));
        return config;
    }

    @Inject
    public void init(Props props) {
        // Check preconditions
        Preconditions.checkNotNull(metadataServiceFacadeImpl, "Inject failed: metadataServiceFacadeImpl is null");
        // load config & boot up
        logger.info("Bootstrapping RPC server for kun-metadata module...");

        RpcConfig config = getMergedConfiguration(props);
        RpcBootstrap.start(config);
        RpcPublishers.exportService(MetadataServiceFacade.class, "1.0", metadataServiceFacadeImpl);
    }
}
