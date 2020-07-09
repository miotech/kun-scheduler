package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.CollectionEntity;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

public class MioArangoClient {

    private ArangoDB client;
    private static Logger logger = LoggerFactory.getLogger(MioArangoClient.class);

    public MioArangoClient(ArangoDataSource cluster){
        this.client = new ArangoDB.Builder()
                .host(cluster.getUrl().split(":")[0], Integer.parseInt(cluster.getUrl().split(":")[1]))
                .user(cluster.getUsername())
                .password(cluster.getPassword())
                .build();
    }

    public Integer count(String dbName, String query){
        try{
            ArangoCursor<Integer> cursor = client.db(dbName).query(query, Integer.class);
            return cursor.hasNext() ? cursor.next() : 0;
        }catch (Exception e){
            logger.error(String.format("query arangodb failed, DB -> %s, query -> %s", dbName, query), e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public String getDoc(String dbName, String query){
        try{
            ArangoCursor<String> cursor = client.db(dbName).query(query, String.class);
            return cursor.hasNext() ? cursor.next() : null;
        }catch (Exception e){
            logger.error(String.format("query arangodb failed, DB -> %s, query -> %s", dbName, query), e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public Collection<String> getDatabases(){
        try{
            return client.getAccessibleDatabases();
        }catch (Exception e) {
            logger.error("get arango accessible db list failed", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public Collection<String> getCollections(String dbName){
        try{
            return client.db(dbName).getCollections().stream().map(CollectionEntity::getName).collect(Collectors.toList());
        }catch (Exception e){
            logger.error("get collection list failed, db -> " + dbName, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
