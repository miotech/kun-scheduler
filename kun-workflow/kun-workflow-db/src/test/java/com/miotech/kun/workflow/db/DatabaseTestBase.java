package com.miotech.kun.workflow.db;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;

public abstract class DatabaseTestBase {
    protected Injector injector = Guice.createInjector(
            new TestDatabaseModule()
    );

    @Before
    public void setUp() throws Exception {
        injector.injectMembers(this);
    }
}
