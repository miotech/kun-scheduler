package com.miotech.kun.metadata.web.rpc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MetadataServiceFacadeImpl implements MetadataServiceFacade {
    private static final Logger logger = LoggerFactory.getLogger(MetadataServiceFacadeImpl.class);

    @Inject
    GidService gidService;

    @Override
    public Dataset getDatasetByDatastore(DataStore datastore) {
        long gid = gidService.generate(datastore);
        logger.debug("fetched gid = {}", gid);
        return null;
    }

    @Override
    public String ping(String msg) {
        return "Pong: " + msg;
    }
}
