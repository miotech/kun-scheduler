package com.miotech.kun.metadata.client;

import com.google.inject.Inject;
import com.miotech.kun.metadata.load.impl.PostgresLoader;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.junit.Test;

public class PostgresLoaderTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator operator;

    @Test
    public void testLoad() {
        PostgresLoader postgresLoader = new PostgresLoader(operator);
        postgresLoader.load(null);

    }
}
