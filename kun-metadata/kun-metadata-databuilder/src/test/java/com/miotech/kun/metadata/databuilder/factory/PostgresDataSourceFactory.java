package com.miotech.kun.metadata.databuilder.factory;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;

public class PostgresDataSourceFactory {

    private PostgresDataSourceFactory() {
    }

    public static PostgresDataSource create() {
        Long id = IdGenerator.getInstance().nextId();
        String host = "127.0.0.1";
        int port = 5432;
        String username = "postgres";
        String password = "postgres";
        return PostgresDataSource.newBuilder()
                .withId(id)
                .withHost(host)
                .withPort(port)
                .withUsername(username)
                .withPassword(password)
                .build();
    }
}
