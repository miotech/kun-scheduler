package com.miotech.kun.dataquality.web.config;

import com.miotech.kun.dataquality.core.hooks.DataQualityCheckHook;
import com.miotech.kun.dataquality.core.model.HookParams;
import com.miotech.kun.dataquality.core.model.OperatorHookParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DataQualityHookConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataQualityHookConfig.class);

    @Value("${data-quality.hooks.check-hook.classname}")
    private String className;

    @Bean
    public DataQualityCheckHook getDataQualityCheckHook(HookParams hookParams) {
        if (className == null) {
            return null;
        }
        try {
            Class clazz = Class.forName(className);
            DataQualityCheckHook dataQualityCheckHook = (DataQualityCheckHook) clazz.
                    getDeclaredConstructor().newInstance();
            dataQualityCheckHook.initialize(hookParams);
            return dataQualityCheckHook;
        } catch (Exception e) {
            logger.error("could not create data quality check hook", e);
            return null;
        }

    }

    @Bean
    @ConfigurationProperties(prefix = "data-quality.hooks.check-hook")
    public HookParams getHookParams() {
        return new HookParams();
    }

    @Bean
    @ConfigurationProperties(prefix = "data-quality.hooks.operator-check-hook")
    public OperatorHookParams getOperatorHookParams() {
        return new OperatorHookParams();
    }


}
