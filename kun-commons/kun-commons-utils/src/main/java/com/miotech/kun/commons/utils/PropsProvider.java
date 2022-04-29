package com.miotech.kun.commons.utils;

import java.util.List;
import java.util.Properties;

public interface PropsProvider {

    <T> T getValue(String key, Class<T> valueType);

    <T> List<T> getValueList(String key, Class<T> valueType);

    boolean containsKey(String key);

    Properties toProperties();
}
