package com.miotech.kun.metadata.databuilder.schedule;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DataBuilderScheduler {
    private static Logger logger = LoggerFactory.getLogger(DataBuilderScheduler.class);
    private static long period = 86400L;
    private static long initialDelay = calculateCurrent2TomorrowSeconds();
    private static final String DEFAULT_ENV = "dev";

    public static void main(String[] args) {
        Map<String, String> paramMap = parseArgs(args);
        Properties props = PropertyUtils.loadAppProps(String.format("application-%s.yaml", paramMap.getOrDefault("env", DEFAULT_ENV)));

        // load datasource
        Injector injector = Guice.createInjector(new DataSourceModule(props));
        DataBuilder dataBuilder = injector.getInstance(DataBuilder.class);
        dataBuilder.scheduleAtRate(Long.parseLong(paramMap.getOrDefault("initialDelay", String.valueOf(initialDelay))), period, TimeUnit.SECONDS);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> paramMap = Maps.newHashMap();
        for (String arg : args) {
            String[] params = arg.split("=");
            paramMap.put(params[0].trim(), params[1].trim());
        }
        return paramMap;
    }

    private static long calculateCurrent2TomorrowSeconds() {
        return LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).toEpochSecond(ZoneOffset.of("+8")) -
                LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
    }

}
