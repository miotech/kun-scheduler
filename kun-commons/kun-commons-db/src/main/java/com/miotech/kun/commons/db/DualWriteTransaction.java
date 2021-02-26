package com.miotech.kun.commons.db;

public interface DualWriteTransaction {

    void doInTransaction(DBOperator dbOperator, ESOperator esOperator);

}
