package com.miotech.kun.datadiscovery.service.rdm.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.base.Preconditions;
import com.miotech.kun.datadiscovery.service.rdm.StorageFileManger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.StringJoiner;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-27 15:34
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class S3StorageFileManger implements StorageFileManger {

    private static String bucketName;

    private final AmazonS3 amazonS3;


    @Value("${rdm.aws.bucket-name:test.ref.data}")
    public void setBucketName(String bucketName) {
        S3StorageFileManger.bucketName = bucketName;
    }

    @Override
    public String putObject(String name, InputStream inputStream) {
        log.debug("s3 pub object path:{}", name);
        PutObjectResult objectResult = amazonS3.putObject(bucketName, name, inputStream, new ObjectMetadata());
        return name;
    }

    @Override
    public InputStream getObjectContent(String name) {
        S3Object object = amazonS3.getObject(bucketName, name);
        return object.getObjectContent();
    }

    public static String concatDataPath(String tableName, Integer tableVersion) {
        return new StringJoiner("/").add("s3:/").add(bucketName).add(concatName(tableName, tableVersion)).toString();
    }

    private static CharSequence concatName(String tableName, Integer tableVersion) {
        Preconditions.checkNotNull(tableName);
        Preconditions.checkNotNull(tableVersion);
        return new StringJoiner("/")
                .add(tableName)
                .add("V" + tableVersion + ".csv").toString();
    }
}
