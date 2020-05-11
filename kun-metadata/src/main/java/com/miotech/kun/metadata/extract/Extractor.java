package com.miotech.kun.metadata.extract;


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
}
