package com.miotech.kun.metadata.client;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Before;
import org.junit.Test;

public class GidServiceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Before
    public void createTable() {
        operator.update("CREATE TABLE kun_mt_dataset_gid (\n" +
                "  \"data_store\" jsonb as json NOT NULL,\n" +
                "  \"dataset_gid\" int8 NOT NULL\n" +
                ")");
    }

    @Test
    public void test() {

    }

}
