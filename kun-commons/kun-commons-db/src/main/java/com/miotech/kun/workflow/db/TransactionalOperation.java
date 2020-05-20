package com.miotech.kun.workflow.db;

@FunctionalInterface
public interface TransactionalOperation<T> {
    public T doInTransaction();
}
