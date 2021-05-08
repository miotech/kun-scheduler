package com.miotech.kun.workflow.testing;

import com.miotech.kun.commons.db.DatabaseSetup;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import org.junit.Before;

import javax.sql.DataSource;

public class WorkflowDatabaseTestBase extends DatabaseTestBase {

    @Before
    public void initDatabase() {
        // initialize database
        dataSource = injector.getInstance(DataSource.class);
        Props props = new Props();
        props.put("flyway.initSql", "CREATE DOMAIN IF NOT EXISTS \"JSONB\" AS TEXT");
        DatabaseSetup setup = new DatabaseSetup(dataSource, props, "workflow-sql/");
        setup.start();
    }
}
