package com.miotech.kun.metadata.databuilder.extract.stat;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.time.LocalDateTime;

public interface DatasetStatExtractor extends DatasetExistenceExtractor {

    Dataset extract(Dataset dataset, DataSource dataSource) throws Exception;

    default LocalDateTime getLastUpdateTime() {
        return null;
    }

}
