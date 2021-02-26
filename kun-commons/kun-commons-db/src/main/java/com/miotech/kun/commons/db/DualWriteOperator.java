package com.miotech.kun.commons.db;

import com.miotech.kun.commons.utils.ExceptionUtils;

public class DualWriteOperator {

    private final DBOperator dbOperator;

    private final ESOperator esOperator;

    public DualWriteOperator(DBOperator dbOperator, ESOperator esOperator) {
        this.dbOperator = dbOperator;
        this.esOperator = esOperator;
    }

    public void transaction(DualWriteTransaction transaction) {
        try {
            begin();
            transaction.doInTransaction(dbOperator, esOperator);
            commit();
        } catch (Exception e) {
            rollback();
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            close();
        }
    }

    private void begin() {
        dbOperator.begin();
    }

    private void commit() {
        dbOperator.commit();
    }

    private void rollback() {
        dbOperator.rollback();
        esOperator.rollback();
    }

    private void close() {
        dbOperator.close();
        esOperator.close();
    }

}
