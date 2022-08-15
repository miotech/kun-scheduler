package com.miotech.kun.datadiscovery.service.rdm.file;

import com.amazonaws.services.s3.AmazonS3;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
public class S3StoragePathGenerator {

    private static String bucketName;

    @Value("${rdm.aws.bucket-name:test.ref.data}")
    private void setBucketName(String bucketName) {
        S3StoragePathGenerator.bucketName = bucketName;
    }

    public static String getVersionDataPath(RefTableVersionInfo refTableVersionInfo) {
        return new StringJoiner("/").add("s3a:/").add(bucketName)
                .add("Database")
                .add(refTableVersionInfo.getDatabaseName())
                .add(refTableVersionInfo.getTableName())
                .add(refTableVersionInfo.getVersionId().toString())
                .add(refTableVersionInfo.getVersionId() + ".parquet").toString();
    }

    public static String getTableVersionPath(RefTableVersionInfo refTableVersionInfo) {
        return new StringJoiner("/").add("s3a:/").add(bucketName)
                .add("Database")
                .add(refTableVersionInfo.getDatabaseName())
                .add(refTableVersionInfo.getTableName())
                .add(refTableVersionInfo.getVersionId() + "/").toString();
    }


}
