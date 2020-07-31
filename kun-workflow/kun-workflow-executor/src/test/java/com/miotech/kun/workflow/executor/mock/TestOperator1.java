package com.miotech.kun.workflow.executor.mock;

import com.google.common.collect.Lists;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.MongoDataStore;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TestOperator1 extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(TestOperator1.class);

    @Override
    public boolean run() {
        final String name = "world";
        logger.info("Hello, {}!", new Object[] { name });
        logger.info("ContextClassLoader: {}", Thread.currentThread().getContextClassLoader());
        report(prepareReport());
        return true;
    }

    private TaskAttemptReport prepareReport() {
        DataStore ds1 = new PostgresDataStore("jdbc:postgresql://10.0.0.1", "test", "", "test1");
        DataStore ds2 = new PostgresDataStore("jdbc:postgresql://10.0.0.1", "test", "", "test2");
        DataStore ds3 = new MongoDataStore("10.0.0.2", "test", "test2");

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
}
