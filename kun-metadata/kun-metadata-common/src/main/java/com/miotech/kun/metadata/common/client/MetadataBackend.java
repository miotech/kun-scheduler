package com.miotech.kun.metadata.common.client;

import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;


public interface MetadataBackend{

    /**
     * return dataset last update time
     * @param dataset
     * @return
     */
    OffsetDateTime getLastUpdatedTime(Dataset dataset);

    /**
     *
     * @param dataset
     * @param judgeMode DATASET:always load dataset from backend,
     *                  SNAPSHOT:use local cache
     * @return
     */
    boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode);

    /**
     * extract dataset fields definition from backend,
     * @param dataset
     * @return
     */
    List<DatasetField> extract(Dataset dataset);

    /**
     * extract all datasets of datasource
     * @param dataSource
     * @return
     */
    Iterator<Dataset> extract(DataSource dataSource);

    /**
     * extract dataset by metadata change event
     * @param mce
     * @return
     */
    Dataset extract(MetadataChangeEvent mce);

    /**
     * get dataset storage lacation
     * @param dataset
     * @return
     */
    String storageLocation(Dataset dataset);

    /**
     * close backend to release resources
     */
    void close();
}
