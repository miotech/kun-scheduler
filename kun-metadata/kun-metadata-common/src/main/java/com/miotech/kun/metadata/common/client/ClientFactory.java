package com.miotech.kun.metadata.common.client;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.AWSGlueClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;

@Singleton
public class ClientFactory {

    public AWSGlue getAWSGlue(String accessKey, String secretKey, String region) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AWSGlueClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(region)).build();
    }

    public AmazonS3 getAmazonS3Client(String accessKey, String secretKey, String region) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.AP_NORTHEAST_1)
                .build();
    }

    public HiveMetaStoreClient getHiveClient(String metaStoreUris) {

        HiveConf conf = new HiveConf();
        conf.setVar(HiveConf.ConfVars.METASTOREURIS, metaStoreUris);
        try {
            return new HiveMetaStoreClient(conf);
        } catch (MetaException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
