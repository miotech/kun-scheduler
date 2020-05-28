package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Elastic Search Extractor
 */
public class ElasticsearchExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(ElasticsearchExtractor.class);

    @Override
    public Iterator<Dataset> extract() {
        return null;
    }

}
