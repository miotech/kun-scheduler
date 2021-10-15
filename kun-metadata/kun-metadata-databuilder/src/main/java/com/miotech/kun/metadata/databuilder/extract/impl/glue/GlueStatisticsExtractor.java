package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.amazonaws.services.glue.model.Table;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.databuilder.client.GlueClient;
import com.miotech.kun.metadata.databuilder.client.S3Client;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.fileparser.ParquetParser;
import com.miotech.kun.metadata.databuilder.extract.statistics.StatisticsExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.MetaStoreParseUtil;
import com.miotech.kun.metadata.databuilder.extract.tool.S3URIParser;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class GlueStatisticsExtractor extends StatisticsExtractorTemplate {

    private static Logger logger = LoggerFactory.getLogger(GlueStatisticsExtractor.class);

    @Override
    public DataWarehouseStatTemplate buildDataWarehouseStatTemplate(Dataset dataset, DataSource dataSource) {
        AWSDataSource awsDataSource = (AWSDataSource) dataSource;
        return new DataWarehouseStatTemplate(dataset.getDatabaseName(), null,
                dataset.getName(), DatabaseType.ATHENA, awsDataSource);
    }

    @Override
    public OffsetDateTime getLastUpdatedTime(Dataset dataset, DataSource dataSource) {
        Table table = GlueClient.searchTable((AWSDataSource) dataSource, dataset.getDatabaseName(), dataset.getName());
        return table == null ? null : table.getUpdateTime().toInstant().atZone(ZoneId.of("UTC")).toOffsetDateTime();
    }

    @Override
    public Long getTotalByteSize(Dataset dataset, DataSource dataSource) {
        AmazonS3 s3Client = null;
        try {
            AWSDataSource awsDataSource = (AWSDataSource) dataSource;
            String location = MetaStoreParseUtil.parseLocation(MetaStoreParseUtil.Type.GLUE, dataSource, dataset.getDatabaseName(), dataset.getName());

            long totalByteSize = 0L;
            s3Client = S3Client.getAmazonS3Client(awsDataSource.getGlueAccessKey(), awsDataSource.getGlueSecretKey(), awsDataSource.getGlueRegion());
            ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(S3URIParser.parseBucketName(location)).withPrefix(S3URIParser.parseKey(location));
            ListObjectsV2Result listing = s3Client.listObjectsV2(req);
            for (S3ObjectSummary summary : listing.getObjectSummaries()) {
                totalByteSize += s3Client.getObjectMetadata(S3URIParser.parseBucketName(location), summary.getKey()).getContentLength();
            }

            return totalByteSize;
        } finally {
            if (s3Client != null) {
                s3Client.shutdown();
            }
        }
    }

    @Override
    public Long getRowCount(Dataset dataset, DataSource dataSource) {
        AWSDataSource awsDataSource = (AWSDataSource) dataSource;

        String outputFormat = MetaStoreParseUtil.parseOutputFormat(MetaStoreParseUtil.Type.GLUE, dataSource, dataset.getDatabaseName(), dataset.getName());
        String location = MetaStoreParseUtil.parseLocation(MetaStoreParseUtil.Type.GLUE, dataSource, dataset.getDatabaseName(), dataset.getName());

        if ("org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat".equals(outputFormat)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Use ParquetParse to get rowCount");
            }

            ParquetParser parquetParser = new ParquetParser();
            return parquetParser.parse(toS3a(location), awsDataSource.getGlueAccessKey(), awsDataSource.getGlueSecretKey()).getRowCount();
        } // Orc and other file formats to be implemented


        // Unable to parse directly from the storage file, use SQL to count the total number of rows
        return super.getRowCount(dataset, dataSource);
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        GlueExistenceExtractor glueExistenceExtractor = new GlueExistenceExtractor();
        return glueExistenceExtractor.judgeExistence(dataset, dataSource, DatasetExistenceJudgeMode.DATASET);
    }

    private String toS3a(String location) {
        return String.format("s3a:%s", location.substring(3));
    }

}
