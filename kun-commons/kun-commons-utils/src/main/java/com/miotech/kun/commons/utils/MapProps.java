package com.miotech.kun.commons.utils;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class MapProps implements PropsProvider {

    private final Map<String, Object> propsMap;

    public MapProps(Map<String, Object> propsMap) {
        this.propsMap = propsMap;
    }

    @Override
    public <T> T getValue(String key, Class<T> valueType) {
        Object value = propsMap.get(key);
        if(value instanceof String){
            if(valueType == Integer.class){
                return (T) Integer.valueOf(value.toString());
            }
            if(valueType == Long.class){
                return (T) Long.valueOf(value.toString());
            }
            if(valueType == Boolean.class){
                return (T) Boolean.valueOf(value.toString());
            }
            if(valueType == Double.class){
                return (T) Double.valueOf(value.toString());
            }
        }
        return (T) value;
    }

    @Override
    public <T> List<T> getValueList(String key, Class<T> valueType) {
        throw new IllegalArgumentException("mapProps not support getValueList");
    }

    @Override
    public boolean containsKey(String key) {
        return propsMap.containsKey(key);
    }

    @Override
    public Properties toProperties() {
        Properties properties = new Properties();
        for (String key : propsMap.keySet()) {
            properties.setProperty(key, propsMap.get(key).toString());
        }
        return properties;
    }

    public Set<String> keySet() {
        return propsMap.keySet();
    }

    public void put(String key, Object value) {
        propsMap.put(key, value);
    }

    @Override
    public String toString() {
        return "MapProps{" +
                "propsMap=" + propsMap +
                '}';
    }
}
