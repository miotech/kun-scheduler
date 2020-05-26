package com.miotech.kun.metadata.client;

import com.google.inject.Inject;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Test;

public class MysqlConnTest {

    @Inject
    private DatabaseOperator operator;

    @Test
    public void testShowDatabases() {
        String type = operator.fetchOne("select `type` from kun_mt_cluster where id = 1", rs -> rs.getString(1));
        System.out.println(type);
    }

}
