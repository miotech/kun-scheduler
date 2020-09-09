package com.miotech.kun.metadata.rpc.provider;

import com.miotech.kun.commons.rpc.RpcPublisher;
import com.miotech.kun.commons.rpc.RpcConfig;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class MetadataRPCServer {
    private static final Logger logger = LoggerFactory.getLogger(MetadataRPCServer.class);

    private static final String APPLICATION_NAME = "METADATA_RPC";

    private static final String RPC_PROPERTIES_FILEPATH = "metadata-rpc.properties";

    private static final Properties properties = new Properties();

    private static void initOverrideConfig() {
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

    private static int getBindPort() {
        return Integer.parseInt(properties.getProperty("metadata.rpc.port"));
    }

    private static RpcConfig initConfiguration() {
        RpcConfig config = new RpcConfig(APPLICATION_NAME);
        config.setPort(getBindPort());
        config.addService(MetadataServiceFacade.class, "1.0", new MetadataServiceFacadeImpl());
        return config;
    }

    public static void main(String[] args) {
        logger.info("Bootstrapping RPC server for kun-metadata module...");
        initOverrideConfig();
        RpcConfig config = initConfiguration();
        RpcPublisher.start(config).await();
    }
}
