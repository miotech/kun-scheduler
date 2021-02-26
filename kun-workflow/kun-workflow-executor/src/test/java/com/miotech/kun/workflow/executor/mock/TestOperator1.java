package com.miotech.kun.workflow.executor.mock;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.core.model.lineage.MongoDataStore;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestOperator1 extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(TestOperator1.class);

    @Override
    public boolean run() {
        final String name = "world";
        logger.info("Hello, {}!", new Object[] { name });
        logger.info("ContextClassLoader: {}", Thread.currentThread().getContextClassLoader());
        Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        report(prepareReport());
        return true;
    }

    private TaskAttemptReport prepareReport() {
        DataStore ds1 = new PostgresDataStore("10.0.0.1", 5432, "test", "", "test1");
        DataStore ds2 = new PostgresDataStore("10.0.0.1", 5432, "test", "", "test2");
        DataStore ds3 = new MongoDataStore("10.0.0.2", 27017, "test", "test2");

        List<DataStore> inlets = Lists.newArrayList(ds1, ds2);
        List<DataStore> outlets = Lists.newArrayList(ds3);

        return TaskAttemptReport.newBuilder()
                .withInlets(inlets)
                .withOutlets(outlets)
                .build();
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public void abort() {

    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }
}
