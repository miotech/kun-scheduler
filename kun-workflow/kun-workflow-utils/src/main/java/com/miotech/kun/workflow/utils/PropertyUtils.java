package com.miotech.kun.workflow.utils;

import com.google.common.io.PatternFilenameFilter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class PropertyUtils {

    public static final String APP_CONFIG_PROPS_PATTERN = "application(-[a-z]+)?\\.yaml";

    public static Properties loadPropsFromResource(String resourceName) {
        Yaml yaml = new Yaml();
        InputStream inputStream = PropertyUtils.class
                .getClassLoader()
                .getResourceAsStream(resourceName);
        Map<String, Object> yamlProps = yaml.load(inputStream);
        Properties properties = new Properties();
        flatten(yamlProps)
                .entrySet()
                .forEach(x -> {
            Object propValue = x.getValue() != null ? x.getValue(): "";
            properties.put(x.getKey(), propValue);
        });
        return properties;
    }

    public static Properties loadAppProps(String applicationConfName) {
        if (applicationConfName != null) {
            return loadPropsFromResource(applicationConfName);
        }

        URL file = PropertyUtils.class.getResource("/");
        File[] configFiles = new File(file.getPath() + "../resources")
                .listFiles(new PatternFilenameFilter(APP_CONFIG_PROPS_PATTERN));
        if (configFiles != null && configFiles.length > 0) {
            return loadPropsFromResource(configFiles[0].getName());
        }
        return new Properties();
    }

    public static Properties loadAppProps() {
        return loadAppProps(null);
    }

    private static Map<String, Object> flatten(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (source == null) return result;
        for (String key : source.keySet()) {
            Object value = source.get(key);

            if (value instanceof Map) {
                Map<String, Object> subMap = flatten((Map<String, Object>) value);

                for (String subkey : subMap.keySet()) {
                    result.put(key + "." + subkey, subMap.get(subkey));
                }
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
}
