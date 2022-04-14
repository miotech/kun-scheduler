package com.miotech.kun.commons.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.PatternFilenameFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropsUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    public static final String APP_CONFIG_PROPS_PATTERN = "application(-[a-z]+)?\\.yaml";
    public static final String APP_CONFIG_ENV = "APP_CONFIG_ENV";
    public static final String APP_CONFIG_FILE = "application.config.file";

    public static final Pattern ENV_CONFIG_PATTERN = Pattern.compile("\\$\\{([^\\}]+)}");

    private PropsUtils() {
    }

    public static Props loadPropsFromResource(String resourceName) {
        logger.info("Loading props from {}", resourceName);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream inputStream = PropertyUtils.class
                .getClassLoader()
                .getResourceAsStream(resourceName);
        mapper.findAndRegisterModules();
        Props props = new Props();
        try {
            JsonNode jsonNode = mapper.readTree(inputStream);
            PropsProvider propsProvider = new JsonProps(mapper,jsonNode);
            props.addPropsProvider(propsProvider);
        }catch (Exception e){
            ExceptionUtils.wrapIfChecked(e);
        }
        return props;
    }

    public static String replaceValueFromEnvironment(String rawText) {
        final Matcher matcher = ENV_CONFIG_PATTERN.matcher(rawText);

        String result = rawText;
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String value = matcher.group(i);
                String defaultValue = null;
                String envKey = value;
                int index = value.indexOf(":");
                if (index > 0) {
                    defaultValue = value.substring(index + 1);
                    envKey = value.substring(0, index);
                }
                String envValue = System.getenv(envKey);
                if (envValue != null) {
                    result = result.replace(String.format("${%s}", value), envValue);
                } else if (defaultValue != null) {
                    result = result.replace(String.format("${%s}", value), defaultValue);
                }
            }
        }
        return result;
    }

    public static void main(String args[]){
       Props props  = loadPropsFromResource("application-local.yaml");

    }

    public static Props loadAppProps(String applicationConfName) {
        if (applicationConfName != null) {
            logger.info("Using application config file: {}", applicationConfName);
            return loadPropsFromResource(applicationConfName);
        }

        String applicationConfigFile = System.getProperty(APP_CONFIG_FILE);
        if (StringUtils.isNotBlank(applicationConfigFile)) {
            return loadAppProps(applicationConfigFile);
        }
        String applicationConfigEnv = System.getenv(APP_CONFIG_ENV);
        if (StringUtils.isNotBlank(applicationConfigEnv)) {
            logger.info("Using application env: {}", applicationConfigEnv);
            return loadAppProps(String.format("application-%s.yaml", applicationConfigEnv));
        }

        URL file = PropertyUtils.class.getResource("/");
        File[] configFiles = new File(file.getPath() + "../resources")
                .listFiles(new PatternFilenameFilter(APP_CONFIG_PROPS_PATTERN));
        if (configFiles != null && configFiles.length > 0) {
            return loadPropsFromResource(configFiles[0].getName());
        }
        return new Props();
    }

    public static Props loadAppProps() {
        return loadAppProps(null);
    }

    private static Map<String, Object> flatten(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (source == null) return result;
        for (String key : source.keySet()) {
            Object value = source.get(key);

            if (value instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) value;
                StringBuilder joiner = new StringBuilder();
                String separator = "";
                for (String subkey : valueMap.keySet()) {
                    joiner.append(separator).append(subkey);
                    separator = ",";
                }
                Map<String, Object> subMap = flatten(valueMap);
                for (String subkey : subMap.keySet()) {
                    result.put(key + "." + subkey, subMap.get(subkey));
                }
                result.put(key, joiner.toString());
            } else if (value instanceof Collection) {
                StringBuilder joiner = new StringBuilder();
                String separator = "";

                for (Object element : ((Collection) value)) {
                    Map<String, Object> subMap = flatten(Collections.singletonMap(key, element));
                    joiner
                            .append(separator)
                            .append(subMap.entrySet().iterator().next().getValue().toString());

                    separator = ",";
                }

                result.put(key, joiner.toString());
            } else {
                result.put(key, value);
            }
        }

        return result;
    }


    public static Props loadPropsFromEnv(String moduleName) {
        Map<String, String> systemEnv = EnvironmentUtils.getVariables();
        //filter kun env
        Map<String, Object> propertiesMap = new HashMap<>();
        systemEnv.entrySet().
                forEach(x -> {
                    String key = x.getKey();
                    if (isKunEnv(key, moduleName)) {
                        String propValue = x.getValue() != null ? x.getValue() : "";
                        propertiesMap.put(convertKey(x.getKey(), moduleName), propValue);
                    }
                });
        PropsProvider propsProvider = new MapProps(propertiesMap);
        Props props = new Props();
        props.addPropsProvider(propsProvider);
        return props;
    }

    public static String convertKey(String envKey, String moduleName) {
        String prefix = "KUN_" + moduleName.toUpperCase() + "_";
        return envKey.substring(prefix.length()).replace('_', '.');
    }

    public static boolean isKunEnv(String envKey, String moduleName) {
        String prefix = "KUN_" + moduleName.toUpperCase() + "_";
        if (envKey.startsWith(prefix)) {
            return true;
        }
        return false;
    }
}
