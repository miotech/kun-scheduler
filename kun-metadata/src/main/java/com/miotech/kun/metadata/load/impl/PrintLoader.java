package com.miotech.kun.metadata.load.impl;

import com.google.gson.Gson;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.model.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class PrintLoader implements Loader {
    private static Logger logger = LoggerFactory.getLogger(PrintLoader.class);
    private static final Gson gson = new Gson();

    @Override
    public void load(Dataset dataset) {
        logger.info("dataset:" + gson.toJson(dataset));
    }
}
