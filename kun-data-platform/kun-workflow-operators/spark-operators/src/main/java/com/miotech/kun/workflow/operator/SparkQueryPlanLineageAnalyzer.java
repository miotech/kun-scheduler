package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.operator.resolver.SparkOperatorResolver;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import static com.miotech.kun.workflow.operator.SparkConfiguration.*;


public class SparkQueryPlanLineageAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(SparkQueryPlanLineageAnalyzer.class);

    public static TaskAttemptReport lineageAnalysis(Config config, Long taskRunId) {
        try {
            TaskAttemptReport.Builder taskAttemptReport = TaskAttemptReport.newBuilder();
            logger.info("start lineage analysis for task {}", taskRunId);
            Configuration conf = new Configuration();

            String configS3AccessKey = config.getString(CONF_S3_ACCESS_KEY);
            if(!Strings.isNullOrEmpty(configS3AccessKey)){
                conf.set("fs.s3a.access.key", configS3AccessKey);
            }
            String configS3SecretKey = config.getString(CONF_S3_SECRET_KEY);
            if(!Strings.isNullOrEmpty(configS3SecretKey)){
                conf.set("fs.s3a.secret.key", configS3SecretKey);
            }

            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

            String hdfsRoot = config.getString(CONF_LINEAGE_OUTPUT_PATH);
            HdfsFileSystem hdfsFileSystem = new HdfsFileSystem(hdfsRoot, conf);
            SparkOperatorResolver resolver = new SparkOperatorResolver(hdfsFileSystem, taskRunId);
            List<DataStore> inputs = resolver.resolveUpstreamDataStore(config);
            List<DataStore> outputs = resolver.resolveDownstreamDataStore(config);
            taskAttemptReport
                    .withInlets(inputs)
                    .withOutlets(outputs);
            return taskAttemptReport.build();
        } catch (Throwable e) {
            logger.warn("lineage analysis failed", e);
        }
        return null;
    }

}
