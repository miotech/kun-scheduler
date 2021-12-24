package com.miotech.kun.metadata.common.client;

import com.miotech.kun.metadata.core.model.dataset.Dataset;

public interface StorageBackend {

    /**
     * get dataset storage size
     * @param dataset
     * @param location
     * @return
     */
    Long getTotalByteSize(Dataset dataset,String location);

}
