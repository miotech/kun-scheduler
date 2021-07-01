package com.miotech.kun.metadata.databuilder.extract;


import com.miotech.kun.metadata.core.model.dataset.Dataset;

import java.util.Iterator;

/**
 * Extractor Definition
 */
public interface Extractor {

    /**
     * Get All the table metadata
     * @return
     */
    Iterator<Dataset> extract() throws Exception;

}
