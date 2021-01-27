package com.miotech.kun.metadata.databuilder.extract.tool;

import com.google.common.base.Preconditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3URIParser {

    private static final String S3_URI_PATTERN = "^s3://(?<bucketName>[\\w\\.]+)/(?<key>[\\w_/]+)$";

    private S3URIParser() {}

    public static String parseBucketName(String uri) {
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

    public static String parseKey(String uri) {
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
