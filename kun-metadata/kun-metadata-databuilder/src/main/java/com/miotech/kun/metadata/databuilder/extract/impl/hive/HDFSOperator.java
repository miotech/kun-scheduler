package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.net.URI;

public class HDFSOperator {

    private HDFSOperator() {
    }

    private static final String SCHEMA = "hdfs";

    public static FileSystem create(String url, Configuration configuration) {
        try {
            return FileSystem.get(URI.create(url), configuration);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static FileSystem create(String url, String user) {
        try {
            Configuration configuration = new Configuration();
            configuration.set("user", user);
            return create(url, configuration);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static FileSystem create(String host, int port, String user) {
        return create(String.format("%s://%s:%d", SCHEMA, host, port), user);
    }

    public static void close(FileSystem fileSystem) {
        try {
            if (fileSystem != null) {
                fileSystem.close();
            }
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

}