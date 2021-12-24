package com.miotech.kun.metadata.common.connector;

import java.sql.ResultSet;

public interface Connector {

    /**
     * return resultSet from query
     * @param query
     * @return
     */
    ResultSet query(Query query);

    /**
     * close connector to release resources
     */
    void close();
}
