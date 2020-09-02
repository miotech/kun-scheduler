package com.miotech.kun.workflow.common.lineage.service;

import com.miotech.kun.commons.rpc.RpcConsumers;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;

import java.util.Optional;

public class DefaultMetadataFacadeConsumer implements MetadataFacade {
    private static volatile boolean consumerStarted;

    // service object should be cached
    private static MetadataServiceFacade metadataService;

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static void init() {
        if (!consumerStarted) {
            metadataService = RpcConsumers.getService(
                    "METADATA_FACADE_CONSUMER",
                    MetadataServiceFacade.class,
                    "1.0"
            );
            consumerStarted = true;
        }
    }

    @Override
    public Optional<Dataset> fetchDatasetByDatastore(DataStore dataStore) {
        init();
        Dataset ds = metadataService.getDatasetByDatastore(dataStore);
        return Optional.ofNullable(ds);
    }

    public String ping() {
        init();
        return metadataService.ping("Are you ok?");
    }

    public static void main(String[] args) {
        DefaultMetadataFacadeConsumer consumer = new DefaultMetadataFacadeConsumer();
        System.out.println(consumer.ping());
    }
}
