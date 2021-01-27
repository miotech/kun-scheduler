package com.miotech.kun.metadata.databuilder.load.impl;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PrintLoader implements Loader {
    private static Logger logger = LoggerFactory.getLogger(PrintLoader.class);

    @Override
    public void loadSchema(Long gid, List<DatasetField> fields) {
        logger.info("loadSchema gid: {}, fields: {}", gid, JSONUtils.toJsonString(fields));
    }

    @Override
    public long loadSchema(Dataset dataset) {
        logger.info("loadSchema dataset: {}", JSONUtils.toJsonString(dataset));
        return -1L;
    }

    @Override
    public void loadStatistics(Dataset dataset) {
        logger.info("loadStat dataset: {}", JSONUtils.toJsonString(dataset));
    }

}
