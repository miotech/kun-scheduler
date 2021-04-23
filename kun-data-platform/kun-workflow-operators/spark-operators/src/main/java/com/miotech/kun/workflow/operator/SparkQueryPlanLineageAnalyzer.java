package com.miotech.kun.workflow.operator;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.operator.resolver.SparkOperatorResolver;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SparkQueryPlanLineageAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(SparkQueryPlanLineageAnalyzer.class);
    private static final String HDFS_ROOT = "s3a://com.miotech.data.prd";

    public static TaskAttemptReport lineageAnalysis(Config config, Long taskRunId) {
        TaskAttemptReport.Builder taskAttemptReport = TaskAttemptReport.newBuilder();
        try {
            logger.info("start lineage analysis for task {}", taskRunId);
            Configuration conf = new Configuration();
            String configS3AccessKey = "fs.s3a.access.key";
            String configS3SecretKey = "fs.s3a.secret.key";
            conf.set(configS3AccessKey, "AKIAVKWKHNJW3VFEZ5XJ");
            conf.set(configS3SecretKey, "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");
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            HdfsFileSystem hdfsFileSystem = new HdfsFileSystem(HDFS_ROOT, conf);
            SparkOperatorResolver resolver = new SparkOperatorResolver(hdfsFileSystem, taskRunId);
            List<DataStore> inputs = resolver.resolveUpstreamDataStore(config);
            List<DataStore> outputs = resolver.resolveDownstreamDataStore(config);
            taskAttemptReport
                    .withInlets(inputs)
                    .withOutlets(outputs);
        } catch (Throwable e) {
            logger.error("create lineage file failed", e);
        }
        return taskAttemptReport.build();
    }

}
