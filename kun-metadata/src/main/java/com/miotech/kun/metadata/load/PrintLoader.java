package com.miotech.kun.metadata.load;

import com.google.gson.Gson;
import com.miotech.kun.metadata.model.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintLoader implements Loader {
    private static Logger logger = LoggerFactory.getLogger(PrintLoader.class);
    
    @Override
    public void load(Dataset dataset) {
        Gson gson = new Gson();
        logger.info("dataset:" + gson.toJson(dataset));
    }
}
