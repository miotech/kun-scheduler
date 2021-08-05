package com.miotech.kun.webapp.listener;

import com.miotech.kun.commons.utils.EnvironmentUtils;
import com.miotech.kun.commons.utils.PropsUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.HashMap;
import java.util.Map;

public class KunAppPropertySourceListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String KUN_APP_PROPERTY_SOURCE_NAME = "kunAppProperties";
    private static final String MODULE_NAME = "APP";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();
        SystemEnvironmentPropertySource kunAppPropertySource = new SystemEnvironmentPropertySource(KUN_APP_PROPERTY_SOURCE_NAME, getKunAppEnv());
        MutablePropertySources sources = env.getPropertySources();
        sources.addFirst(kunAppPropertySource);
    }

    private Map<String, Object> getKunAppEnv() {
        Map<String, String> systemEnv = EnvironmentUtils.getVariables();
        Map<String, Object> appEnv = new HashMap<>();
        //filter kun env
        systemEnv.entrySet().
                forEach(x -> {
                    String key = x.getKey();
                    if (PropsUtils.isKunEnv(key, MODULE_NAME)) {
                        String propValue = x.getValue() != null ? x.getValue() : "";
                        appEnv.put(PropsUtils.convertKey(key, MODULE_NAME), propValue);
                    }
                });
        return appEnv;
    }
}
