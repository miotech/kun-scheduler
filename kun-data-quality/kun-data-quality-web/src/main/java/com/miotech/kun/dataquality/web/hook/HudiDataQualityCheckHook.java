package com.miotech.kun.dataquality.web.hook;

import com.miotech.kun.commons.utils.HdfsFileSystem;
import com.miotech.kun.dataquality.core.hooks.DataQualityCheckHook;
import com.miotech.kun.dataquality.core.model.DataQualityContext;
import com.miotech.kun.dataquality.core.model.HookParams;
import com.miotech.kun.dataquality.core.model.ValidateResult;
import com.miotech.kun.dataquality.core.service.HudiService;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;


public class HudiDataQualityCheckHook implements DataQualityCheckHook {

    private static final Logger logger = LoggerFactory.getLogger(HudiDataQualityCheckHook.class);

    private HudiService hudiService;

    @Override
    public void initialize(HookParams hookParams) {
        Map<String, String> parms = hookParams.getParams();
        try {
            HdfsFileSystem hdfsFileSystem = initFileSystem(parms);
            hudiService = new HudiService(hdfsFileSystem);
        } catch (Exception e) {
            logger.error("init HudiDataQualityCheckHook failed", e);
        }
    }

    @Override
    public boolean afterAll(DataQualityContext context) {
        try {
            logger.debug("execute hook after all case finished context = {}",context);
            Dataset dataset = context.getDataset();
            String version = hudiService.getValidatingVersion(dataset.getDatabaseName(), dataset.getName());
            if (context.getValidateResult().equals(ValidateResult.SUCCESS) && version != null) {
                upgradeVersion(dataset, version);
            }
        } catch (Exception e) {
            logger.warn("execute hook after all case finished is failed", e);
        }

        return true;
    }

    private void upgradeVersion(Dataset dataset, String version) {
        logger.debug("going to upgrade dataset : {} to version : {}", dataset.getName(), version);
        try {
            hudiService.transitToValidated(dataset.getDatabaseName(), dataset.getName(), version);
        } catch (IOException e) {
            logger.error("failed to upgrade dataset : {} to version : {} ", dataset.getName(), version);
        }
    }

    private HdfsFileSystem initFileSystem(Map<String, String> params) throws Exception {
        Configuration conf = new Configuration();
        String configS3AccessKey = params.get("access-key");
        String configS3SecretKey = params.get("secrey-key");
        String warehouseUrl = params.get("warehouse-url");

        conf.set("fs.s3a.access.key", configS3AccessKey);
        conf.set("fs.s3a.secret.key", configS3SecretKey);
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

        return new HdfsFileSystem(warehouseUrl, conf);
    }
}
