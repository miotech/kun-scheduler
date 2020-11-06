package com.miotech.kun.metadata.web.rpc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.common.service.MetadataDatasourceService;
import com.miotech.kun.metadata.core.model.DSI;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetBaseInfo;
import com.miotech.kun.metadata.core.model.dto.DataSourceDTO;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Singleton
public class MetadataServiceFacadeImpl implements MetadataServiceFacade {
    private static final Logger logger = LoggerFactory.getLogger(MetadataServiceFacadeImpl.class);

    @Inject
    GidService gidService;

    @Inject
    MetadataDatasetService metadataDatasetService;

    @Inject
    MetadataDatasourceService metadataDatasourceService;

    @Override
    public Dataset getDatasetByDatastore(DataStore datastore) {
        long gid = gidService.generate(datastore);
        logger.debug("fetched gid = {}", gid);

        Optional<Dataset> datasetOptional = metadataDatasetService.fetchDatasetByGid(gid);
        if (datasetOptional.isPresent()) {
            return datasetOptional.get();
        } else {
            return null;
        }
    }

    @Override
    public List<DatasetBaseInfo> fetchDatasetsByDatasourceAndNameLike(Long datasourceId, String name) {
        return metadataDatasetService.fetchDatasetsByDatasourceAndNameLike(datasourceId, name);
    }

    @Override
    public DataSourceDTO getDataSourceById(Long datasourceId) {
        return metadataDatasourceService.getDataSourceById(datasourceId);
    }
}
