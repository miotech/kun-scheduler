package com.miotech.kun.metadata.common.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.core.model.connection.S3ConnectionInfo;
import com.miotech.kun.metadata.core.model.dataset.Dataset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3Backend implements StorageBackend {

    private final S3ConnectionInfo connectionInfo;
    private final AmazonS3 s3Client;
    private static final String S3_URI_PATTERN = "^(s3|s3a|s3n)://(?<bucketName>[\\w\\.]+)/(?<key>[\\w_/]+)$";
    private final ClientFactory clientFactory;

    public S3Backend(S3ConnectionInfo connectionInfo,ClientFactory clientFactory) {
        this.connectionInfo = connectionInfo;
        this.clientFactory = clientFactory;
        s3Client = clientFactory.getAmazonS3Client(connectionInfo.getS3AccessKey(), connectionInfo.getS3SecretKey(), connectionInfo.getS3Region());
    }

    @Override
    public Long getTotalByteSize(Dataset dataset, String location) {
        try {
            long totalByteSize = 0L;
            ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(parseBucketName(location)).withPrefix(parseKey(location));
            ListObjectsV2Result listing = s3Client.listObjectsV2(req);
            for (S3ObjectSummary summary : listing.getObjectSummaries()) {
                totalByteSize += s3Client.getObjectMetadata(parseBucketName(location), summary.getKey()).getContentLength();
            }

            return totalByteSize;
        } finally {
            if (s3Client != null) {
                s3Client.shutdown();
            }
        }
    }

    private String parseBucketName(String uri) {
        Preconditions.checkNotNull(uri);

        if (!uri.startsWith("s3")) {
            throw new IllegalArgumentException("Invalid uri: " + uri);
        }

        Matcher matcher = Pattern.compile(S3_URI_PATTERN).matcher(uri);
        if (matcher.find()) {
            int bucketNameStart = matcher.start("bucketName");
            int bucketNameEnd = matcher.end("bucketName");

            return uri.substring(bucketNameStart, bucketNameEnd);
        }

        throw new IllegalStateException("Unable to parse uri: " + uri);
    }

    private String parseKey(String uri) {
        Preconditions.checkNotNull(uri);

        if (!uri.startsWith("s3")) {
            throw new IllegalArgumentException("Invalid uri: " + uri);
        }

        Matcher matcher = Pattern.compile(S3_URI_PATTERN).matcher(uri);
        if (matcher.find()) {
            int keyStart = matcher.start("key");
            int keyEnd = matcher.end("key");
            return uri.substring(keyStart, keyEnd);
        }

        throw new IllegalStateException("Unable to parse uri: " + uri);
    }
}
