package com.miotech.kun.workflow.common;

import com.google.inject.AbstractModule;
import com.miotech.kun.workflow.db.DatabaseModule;

public class CommonModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new DatabaseModule());
    }
}
