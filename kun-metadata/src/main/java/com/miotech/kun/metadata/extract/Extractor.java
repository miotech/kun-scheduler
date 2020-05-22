package com.miotech.kun.metadata.extract;


import com.miotech.kun.metadata.model.Dataset;

import java.util.Iterator;

/**
 * Extractor Definition
 */
public interface Extractor {

    /**
     * Get All the table metadata
     * @return
     */
    Iterator<Dataset> extract();

}
