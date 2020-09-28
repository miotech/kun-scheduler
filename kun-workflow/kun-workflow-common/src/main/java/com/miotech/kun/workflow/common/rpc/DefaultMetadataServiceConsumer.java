package com.miotech.kun.workflow.common.rpc;

import com.google.inject.Singleton;
import com.miotech.kun.commons.rpc.RpcConsumers;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class DefaultMetadataServiceConsumer implements MetadataServiceFacade {
    private static final AtomicBoolean consumerStarted = new AtomicBoolean(false);

    // service object stub should be cached
    private static MetadataServiceFacade remoteMetadataServiceStub;

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static void init() {
        if (!consumerStarted.get()) {
            remoteMetadataServiceStub = RpcConsumers.getService(
                    "METADATA_FACADE_CONSUMER",
                    MetadataServiceFacade.class,
                    "1.0"
            );
            consumerStarted.compareAndSet(false, true);
        }
    }

    @Override
    public Dataset getDatasetByDatastore(DataStore datastore) {
        init();
        return remoteMetadataServiceStub.getDatasetByDatastore(datastore);
    }
}
