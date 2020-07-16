package com.miotech.kun.commons.db;

@FunctionalInterface
public interface TransactionalOperation<T> {
    public T doInTransaction();
}
