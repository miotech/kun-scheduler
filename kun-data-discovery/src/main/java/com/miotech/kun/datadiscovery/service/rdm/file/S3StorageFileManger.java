package com.miotech.kun.datadiscovery.service.rdm.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.service.rdm.StorageFileManger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private void setBucketName(String bucketName) {
        S3StorageFileManger.bucketName = bucketName;
    }

    @Override
    public String putObject(String path, InputStream inputStream) {
        log.debug("s3 put object path:{}", path);
        String relativePath = relativePath(path);
        log.debug("s3 put relative path:{}", relativePath);
        amazonS3.putObject(bucketName, relativePath, inputStream, new ObjectMetadata());
        return path;
    }

    public static String relativePath(String path) {
        if (StringUtils.isNotBlank(path)) {
            return path.substring(path.indexOf(bucketName) + bucketName.length() + 1);
        }
        return "";
    }

    @Override
    public InputStream getObjectContent(String path) {
        log.debug("s3 get object path:{}", path);
        String relativePath = relativePath(path);
        log.debug("s3 get relative path:{}", relativePath);
        S3Object object = amazonS3.getObject(bucketName, relativePath);
        return object.getObjectContent();
    }

    public static String getVersionDataPath(RefTableVersionInfo refTableVersionInfo) {
        return new StringJoiner("/").add("s3:/").add(bucketName)
                .add("Database")
                .add(refTableVersionInfo.getDatabaseName())
                .add(refTableVersionInfo.getTableName())
                .add(refTableVersionInfo.getVersionId().toString())
                .add(refTableVersionInfo.getVersionId() + ".csv").toString();
    }

    public static String getTableVersionPath(RefTableVersionInfo refTableVersionInfo) {
        return new StringJoiner("/").add("s3:/").add(bucketName)
                .add("Database")
                .add(refTableVersionInfo.getDatabaseName())
                .add(refTableVersionInfo.getTableName())
                .add(refTableVersionInfo.getVersionId() + "/").toString();
    }


}
