package com.miotech.kun.workflow.db;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionalOperation<T> {
    public T doInTransaction(DatabaseOperator databaseOperator) throws SQLException;
}
