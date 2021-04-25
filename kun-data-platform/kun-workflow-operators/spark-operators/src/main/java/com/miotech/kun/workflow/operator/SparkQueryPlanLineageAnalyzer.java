package com.miotech.kun.workflow.operator;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.operator.resolver.SparkOperatorResolver;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class SparkQueryPlanLineageAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(SparkQueryPlanLineageAnalyzer.class);

    public static TaskAttemptReport lineageAnalysis(Config config, Long taskRunId, Map<String, String> sparkConf) {
        TaskAttemptReport.Builder taskAttemptReport = TaskAttemptReport.newBuilder();
        try {
            logger.info("start lineage analysis for task {}", taskRunId);
            Configuration conf = new Configuration();

            if(sparkConf.containsKey("spark.fs.s3a.access.key")){
                String configS3AccessKey = sparkConf.get("spark.fs.s3a.access.key");
                conf.set("fs.s3a.access.key", configS3AccessKey);
            }
            if(sparkConf.containsKey("spark.fs.s3a.secret.key")){
                String configS3SecretKey = sparkConf.get("spark.fs.s3a.secret.key");
                conf.set("fs.s3a.secret.key", configS3SecretKey);
            }

            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

            String hdfsRoot = sparkConf.get("spark.hadoop.spline.hdfs_dispatcher.address");
            HdfsFileSystem hdfsFileSystem = new HdfsFileSystem(hdfsRoot, conf);
            SparkOperatorResolver resolver = new SparkOperatorResolver(hdfsFileSystem, taskRunId);
            List<DataStore> inputs = resolver.resolveUpstreamDataStore(config);
            List<DataStore> outputs = resolver.resolveDownstreamDataStore(config);
            taskAttemptReport
                    .withInlets(inputs)
                    .withOutlets(outputs);
        } catch (Throwable e) {
            logger.error("lineage analysis failed", e);
        }
        return taskAttemptReport.build();
    }

}
