package com.miotech.kun.workflow.common;

import com.miotech.kun.commons.testing.DatabaseTestBase;

import java.util.Properties;

public class CommonTestBase extends DatabaseTestBase {

    @Override
    protected void configuration() {
        super.configuration();
        Properties props = new Properties();
        addModules(new CommonTestModule(props));
    }
}
