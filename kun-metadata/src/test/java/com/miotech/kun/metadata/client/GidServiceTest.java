package com.miotech.kun.metadata.client;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Before;
import org.junit.Ignore;

@Ignore
public class GidServiceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Before
    public void createTable() {

    }

}
