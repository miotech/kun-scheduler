package com.miotech.kun.metadata.load.impl;

import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class PrintLoader implements Loader {
    private static Logger logger = LoggerFactory.getLogger(PrintLoader.class);

    @Override
    public void load(Dataset dataset) {
        logger.info("dataset:" + JSONUtils.toJsonString(dataset));
    }
}
