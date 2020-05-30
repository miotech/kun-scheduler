package com.miotech.kun.metadata.schedule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

public class DataBuilderScheduler {
    private static Logger logger = LoggerFactory.getLogger(DataBuilderScheduler.class);
    private static final long period = 86400L;
    private static long initialDelay = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).toEpochSecond(ZoneOffset.of("+8")) - LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));

    public static void main(String[] args) {
        try {
            if (args != null && args.length == 1) {
                initialDelay = Long.parseLong(args[0]);
            }
        } catch (NumberFormatException numberFormatException) {
            logger.error("initialDelay should be number");
            throw ExceptionUtils.wrapIfChecked(numberFormatException);
        }

        // load datasource
        Injector injector = Guice.createInjector(new DataSourceModule());
        DataBuilder dataBuilder = injector.getInstance(DataBuilder.class);
        dataBuilder.scheduleAtRate(initialDelay, period, TimeUnit.SECONDS);
    }
}
