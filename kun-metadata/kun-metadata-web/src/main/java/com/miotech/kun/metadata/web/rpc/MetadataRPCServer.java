package com.miotech.kun.metadata.web.rpc;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.miotech.kun.commons.rpc.RpcBootstrap;
import com.miotech.kun.commons.rpc.RpcConfig;
import com.miotech.kun.commons.rpc.RpcPublishers;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class MetadataRPCServer {
    private final Logger logger = LoggerFactory.getLogger(MetadataRPCServer.class);

    private final String APPLICATION_NAME = "METADATA_RPC";

    private final String RPC_PROPERTIES_FILEPATH = "metadata-rpc.properties";

    private final Properties properties = new Properties();

    @Inject
    private MetadataServiceFacadeImpl metadataServiceFacadeImpl;

    private void initOverrideConfig() {
        InputStream inputStream = MetadataRPCServer.class.getClassLoader().getResourceAsStream(RPC_PROPERTIES_FILEPATH);
        if (Objects.nonNull(inputStream)) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        } else {
            throw new RuntimeException(String.format("Cannot load RPC properties configuration file: %s", RPC_PROPERTIES_FILEPATH));
        }
    }

    private int getBindPort() {
        return Integer.parseInt(properties.getProperty("metadata.rpc.port"));
    }

    private RpcConfig getMergedConfiguration() {
        RpcConfig config = new RpcConfig(APPLICATION_NAME);
        config.setPort(getBindPort());
        return config;
    }

    @Inject
    public void init() {
        // Check preconditions
        Preconditions.checkNotNull(metadataServiceFacadeImpl, "Inject failed: metadataServiceFacadeImpl is null");
        // load config & boot up
        logger.info("Bootstrapping RPC server for kun-metadata module...");
        initOverrideConfig();
        RpcConfig config = getMergedConfiguration();
        RpcBootstrap.start(config);
        RpcPublishers.exportService(MetadataServiceFacade.class, "1.0", metadataServiceFacadeImpl);
    }
}
