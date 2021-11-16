package com.miotech.kun.metadata.common.cataloger;

import com.miotech.kun.metadata.common.client.MetadataBackend;
import com.miotech.kun.metadata.common.client.StorageBackend;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;

public class Cataloger {

    private final MetadataBackend metadataBackend;
    private final StorageBackend storageBackend;

    public Cataloger(MetadataBackend metadataBackend, StorageBackend storageBackend) {
        this.metadataBackend = metadataBackend;
        this.storageBackend = storageBackend;
    }

    public OffsetDateTime getLastUpdatedTime(Dataset dataset) {
        return metadataBackend.getLastUpdatedTime(dataset);
    }

    public boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode) {
        return metadataBackend.judgeExistence(dataset, judgeMode);
    }

    public Long getTotalByteSize(Dataset dataset) {
        String location = metadataBackend.storageLocation(dataset);
        return storageBackend.getTotalByteSize(dataset, location);
    }

    public List<DatasetField> extract(Dataset dataset) {
        return metadataBackend.extract(dataset);
    }

    public Iterator<Dataset> extract(DataSource dataSource) {
        return metadataBackend.extract(dataSource);
    }

    public Dataset extract(MetadataChangeEvent mce) {
        return metadataBackend.extract(mce);
    }

    public void close(){
        metadataBackend.close();
    }

}
