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
    private static String env = "dev";

    public static void main(String[] args) {
        int length = args.length;
        if (length == 1) {
            env = String.format("application-%s.conf", args[0]);
        } else if (length == 2) {
            env = String.format("application-%s.conf", args[0]);
            try {
                initialDelay = Long.parseLong(args[1]);
            } catch (NumberFormatException numberFormatException) {
                logger.error("[initialDelay] should be number");
                throw ExceptionUtils.wrapIfChecked(numberFormatException);
            }
        } else {
            throw new IllegalArgumentException("Param error, Usage [env] [initialDelay]");
        }

        // load datasource
        Injector injector = Guice.createInjector(new DataSourceModule(env));
        DataBuilder dataBuilder = injector.getInstance(DataBuilder.class);
        dataBuilder.scheduleAtRate(initialDelay, period, TimeUnit.SECONDS);
    }
}
