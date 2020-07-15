package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An operator simulating a task with 5 seconds execution duration
 */
public class TestOperator4 extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(TestOperator4.class);

    private AtomicBoolean aborted = new AtomicBoolean(false);

    @SuppressWarnings("java:S2925")
    public boolean run() {
        OffsetDateTime start = DateTimeUtils.now();
        while (true) {
            OffsetDateTime now = DateTimeUtils.now();
            Duration duration = Duration.between(start, now);
            if (aborted.get() || duration.getSeconds() > 10L) {
                logger.info("TestOperator4 is aborting...");
                break;
            }
        }
        return true;
    }

    @Override
    public void abort() {
        aborted.compareAndSet(false, true);
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }
}
