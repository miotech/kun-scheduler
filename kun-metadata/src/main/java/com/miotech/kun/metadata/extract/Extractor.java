package com.miotech.kun.metadata.extract;


import com.miotech.kun.metadata.model.bo.*;
import com.miotech.kun.metadata.models.Table;

import java.util.List;

/**
 * Extractor Definition
 */
public interface Extractor {
    /**
     * Get All the table metadata
     * @return
     */
    List<Table> extract();

    /**
     * Get the information of all tables under the database
     * @param extractBO
     * @return
     */
    List<DatasetInfo> extractDataset(DatasetExtractBO extractBO);

    /**
     * Get all field information in dataset
     * @param fieldExtractBO
     * @return
     */
    List<DatasetFieldInfo> extractFields(DatasetFieldExtractBO fieldExtractBO);

    /**
     * Get statistics of dataset
     * @param statisticsExtractBO
     * @return
     */
    DatasetStatisticsInfo extractDatasetStatistics(DatasetStatisticsExtractBO statisticsExtractBO);

    /**
     * Get statistics for a single field of dataset
     * @param fieldStatisticsExtractBO
     * @return
     */
    DatasetFieldStatisticsInfo extractDatasetFieldStatistics(DatasetFieldStatisticsExtractBO fieldStatisticsExtractBO);
}
