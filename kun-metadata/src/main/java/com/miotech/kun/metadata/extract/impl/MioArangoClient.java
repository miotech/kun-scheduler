package com.miotech.kun.metadata.extract.impl;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.model.entity.CommonCluster;

import java.util.Collection;
import java.util.stream.Collectors;

@Singleton
public class MioArangoClient {

    private ArangoDB client;

    @Inject
    public MioArangoClient(CommonCluster cluster){
        this.client = new ArangoDB.Builder()
                .host(cluster.getHostname(), cluster.getPort())
                .user(cluster.getUsername())
                .password(cluster.getPassword())
                .build();
    }

    public Integer count(String dbName, String query){
        ArangoCursor<Integer> cursor2 = client.db(dbName).query(query, Integer.class);
        return cursor2.next();
    }

    public String getDoc(String dbName, String query){
        ArangoCursor<String> cursor = client.db(dbName).query(query, String.class);
        return cursor.next();
    }

    public Collection<String> getDatabases(){
        return client.getAccessibleDatabases();
    }

    public Collection<String> getCollections(String dbName){
        return client.db(dbName).getCollections().stream().map(entity -> entity.getName()).collect(Collectors.toList());
    }
}
