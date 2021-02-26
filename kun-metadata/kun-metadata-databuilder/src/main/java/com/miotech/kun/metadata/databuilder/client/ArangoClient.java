package com.miotech.kun.metadata.databuilder.client;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.CollectionEntity;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;

import java.util.Collection;
import java.util.stream.Collectors;

public class ArangoClient {

    private final ArangoDB client;

    public ArangoClient(ArangoDataSource dataSource) {
        this.client = new ArangoDB.Builder()
                .host(dataSource.getHost(), dataSource.getPort())
                .user(dataSource.getUsername())
                .password(dataSource.getPassword())
                .build();
    }

    public ArangoClient(String host, int port, String user, String password) {
        this.client = new ArangoDB.Builder()
                .host(host, port)
                .user(user)
                .password(password)
                .build();
    }

    public Integer count(String dbName, String query) {
        ArangoCursor<Integer> cursor = client.db(dbName).query(query, Integer.class);
        return cursor.hasNext() ? cursor.next() : 0;
    }

    public String getDoc(String dbName, String query) {
        ArangoCursor<String> cursor = client.db(dbName).query(query, String.class);
        return cursor.hasNext() ? cursor.next() : null;
    }

    public Collection<String> getDatabases() {
        return client.getAccessibleDatabases();
    }

    public Collection<String> getCollections(String dbName) {
        return client.db(dbName).getCollections().stream().map(CollectionEntity::getName).collect(Collectors.toList());
    }

    public boolean judgeTableExistence(String dbName, String name) {
        boolean dbExisted = client.db(dbName).exists();
        if (!dbExisted) {
            return false;
        }

        return client.db(dbName).collection(name).exists();
    }

    public void close() {
        client.shutdown();
    }

}
