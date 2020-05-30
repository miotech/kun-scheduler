package com.miotech.kun.metadata.schedule;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class DataBuilderScheduler {
    private static Logger logger = LoggerFactory.getLogger(DataBuilderScheduler.class);

    @Inject
    private DataBuilder dataBuilder;

    public static void main(String[] args) {
        long period = Long.MAX_VALUE;
        Preconditions.checkState(args != null && args.length == 1, "period should not be null");
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException numberFormatException) {
            logger.error("period should be number: " + args[0]);
            throw ExceptionUtils.wrapIfChecked(numberFormatException);
        }
        period = Long.parseLong(args[0]);

        // load datasource
        DataBuilderScheduler scheduler = new DataBuilderScheduler();
        scheduler.loadDataSource();

        scheduler.dataBuilder.scheduleAtRate(period, TimeUnit.SECONDS);
    }

    private void loadDataSource() {
        Injector injector = Guice.createInjector(new DataSourceModule());
        injector.injectMembers(this);
    }

}
