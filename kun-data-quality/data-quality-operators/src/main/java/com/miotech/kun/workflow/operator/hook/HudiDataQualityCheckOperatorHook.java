package com.miotech.kun.workflow.operator.hook;

import com.miotech.kun.commons.utils.HdfsFileSystem;
import com.miotech.kun.dataquality.core.hooks.DataQualityCheckOperationHook;
import com.miotech.kun.dataquality.core.model.DataQualityOperatorContext;
import com.miotech.kun.dataquality.core.model.OperatorHookParams;
import com.miotech.kun.dataquality.core.service.HudiService;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class HudiDataQualityCheckOperatorHook implements DataQualityCheckOperationHook {

    private static final Logger logger = LoggerFactory.getLogger(HudiDataQualityCheckOperatorHook.class);

    private HudiService hudiService;

    //record the version used to be validating,and has been transit to validated in before() method
    private String validatingVersion;

    @Override
    public void initialize(OperatorHookParams hookParams) {
        Map<String, String> parms = hookParams.getParams();
        try {
            HdfsFileSystem hdfsFileSystem = initFileSystem(parms);
            hudiService = new HudiService(hdfsFileSystem);
        } catch (Exception e) {
            logger.error("init HudiDataQualityCheckHook failed", e);
        }
    }

    @Override
    public boolean before(DataQualityOperatorContext context) {
        Dataset dataset = context.getDataset();
        String database = dataset.getDatabaseName();
        String table = dataset.getName();
        try {
            String version = hudiService.getValidatingVersion(database, table);
            if (version == null) {
                return true;
            }
            logger.debug("going to transit version : {} to validated for database: {} table: {}", version, database, table);
            hudiService.transitToValidated(dataset.getDatabaseName(), dataset.getName(), version);
            validatingVersion = version;
        } catch (IOException e) {
            logger.error("failed transit hudi table {}:{} from validating to validated", database, table, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean after(DataQualityOperatorContext context) {
        Dataset dataset = context.getDataset();
        String database = dataset.getDatabaseName();
        String table = dataset.getName();
        if (validatingVersion == null) {
            return true;
        }
        try {
            logger.debug("going to transit version : {} to validating for database: {} table: {}", validatingVersion, database, table);
            hudiService.transitToValidating(dataset.getDatabaseName(), dataset.getName(), validatingVersion);
        } catch (IOException e) {
            logger.error("failed transit hudi version : {} from validated to validating", validatingVersion, e);
            return false;
        }
        return true;
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
