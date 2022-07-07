package com.miotech.kun.datadiscovery.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.AWSGlueClientBuilder;
import com.amazonaws.services.glue.model.EntityNotFoundException;
import com.amazonaws.services.glue.model.GetDatabaseRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-07-04 14:00
 **/
@Configuration
@ConditionalOnExpression("${discovery.enabled:true}")
public class AWSClientConfig {
    @Value("${rdm.aws.access-key:accessKey}")
    private String accessKey;
    @Value("${rdm.aws.secret-key:secretKey}")
    private String secretKey;
    @Value("${rdm.aws.region:region}")
    private String region;
    @Value("${rdm.aws.bucket-name:test.ref.data}")
    private String bucketName;
    @Value("${rdm.database-name:default}")
    private String databaseName;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.AP_NORTHEAST_1)
                .build();
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            amazonS3.createBucket(bucketName);
        }
        return amazonS3;
    }


    @Bean
    public AWSGlue awsGlue() {
        AWSGlue awsGlue = AWSGlueClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withRegion(region)
                .build();
        try {
            awsGlue.getDatabase(new GetDatabaseRequest().withName(databaseName));
        } catch (EntityNotFoundException e) {
            throw new IllegalStateException(String.format("database  not found:%s", databaseName), e);
        }

        return awsGlue;
    }
}
