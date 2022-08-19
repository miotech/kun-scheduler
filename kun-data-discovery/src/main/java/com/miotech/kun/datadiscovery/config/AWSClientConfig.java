package com.miotech.kun.datadiscovery.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.AWSGlueClientBuilder;
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


    @Bean(name = "filSystemConfiguration")
    public org.apache.hadoop.conf.Configuration filSystemConfiguration() {
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider");
        configuration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        configuration.set("fs.s3a.access.key", accessKey);
        configuration.set("fs.s3a.secret.key", secretKey);
        return configuration;
    }


    @Bean
    public AWSGlue awsGlue() {
        return AWSGlueClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withRegion(region)
                .build();
    }
}
