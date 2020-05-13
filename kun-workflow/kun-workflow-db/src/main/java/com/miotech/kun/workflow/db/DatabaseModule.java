package com.miotech.kun.workflow.db;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.sql.DataSource;

public class DatabaseModule extends AbstractModule {
    @Provides
    @Singleton
    public DataSource createDataSource() {
        return null;
    }
}
