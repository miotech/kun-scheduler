package com.miotech.kun.metadata.rpc.provider;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MetadataServiceFacadeImpl implements MetadataServiceFacade {
    private static final Logger logger = LoggerFactory.getLogger(MetadataServiceFacadeImpl.class);

    @Override
    public Dataset getDatasetByDatastore(DataStore datastore) {
        return null;
    }

    @Override
    public String ping(String msg) {
        return "Pong: " + msg;
    }
}
