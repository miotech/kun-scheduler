package com.miotech.kun.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Hashmap implementation of a hierarchical properties with helpful converter functions and
 * Exception throwing. This class is not threadsafe.
 */
public class Props {

    private static final Logger logger = LoggerFactory.getLogger(Props.class);

    private final List<Map<String, String>> propertiesList;

    public static Props fromProperties(Properties properties) {
        return new Props(properties);
    }

    public Props() {
        propertiesList = new ArrayList<>();
    }

    public Props(final Properties... properties) {
        this();
        for (int i = properties.length - 1; i >= 0; i--) {
            this.put(properties[i]);
        }
    }

    /**
     * Create properties from maps of properties
     */
    public Props(final Map<String, String>... props) {
        this();
        for (int i = props.length - 1; i >= 0; i--) {
            propertiesList.add(props[i]);
        }
    }

    /**
     * Put the given Properties into the Props. This method performs any variable substitution in the
     * value replacing any occurrence of ${name} with the value of get("name"). get() is called first
     * on the Props and next on the Properties object.
     *
     * @param properties The properties to put
     * @throws IllegalArgumentException If the variable given for substitution is not a valid key in
     *                                  this Props.
     */
    public void put(final Properties properties) {
        for (final String propName : properties.stringPropertyNames()) {
            put(propName, properties.getProperty(propName));
        }
    }

    /**
     * put another props into this props，values of the same key will be overwritten
     *
     * @param props
     */
    public void addProps(final Props props) {
        for (int i = props.propertiesList.size() - 1; i >= 0; i--) {
            propertiesList.add(0, props.propertiesList.get(i));
        }
    }

    /**
     * Put object
     */
    public void put(final String key, final Object value) {
        if (propertiesList.size() == 0) {
            Map<String, String> propertiesMap = new HashMap<>();
            propertiesMap.put(key, value.toString());
            propertiesList.add(propertiesMap);
        } else {
            propertiesList.get(0).put(key, value.toString());
        }
    }

    /**
     * Check key in propertiesMap
     */
    public boolean containsKey(final Object key) {
        for (Map<String, String> propertiesMap : propertiesList) {
            String upCaseKey = key.toString().toUpperCase();
            if (propertiesMap.containsKey(key) || propertiesMap.containsKey(upCaseKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return value if available in properties
     *
     * @return
     */
    public String get(final Object key) {
        for (int i = 0; i < propertiesList.size(); i++) {
            Map<String, String> propertiesMap = propertiesList.get(i);
            if (propertiesMap.containsKey(key)) {
                return propertiesMap.get(key);
            }
            String upCaseKey = key.toString().toUpperCase();
            if (propertiesMap.containsKey(upCaseKey)) {
                return propertiesMap.get(upCaseKey);
            }
        }
        return null;

    }

    /**
     * Gets the string from the Props. If it doesn't exist, it will return the defaultValue
     */
    public String getString(final String key, final String defaultValue) {
        if (containsKey(key)) {
            return get(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets the string from the Props. If it doesn't exist, throw and UndefinedPropertiesException
     */
    public String getString(final String key) {
        if (containsKey(key)) {
            return get(key);
        } else {
            throw new UndefinedPropertyException("Missing required property '" + key
                    + "'");
        }
    }

    /**
     * Returns true if the value equals "true". If the value is null, then the default value is
     * returned.
     */
    public boolean getBoolean(final String key, final boolean defaultValue) {
        if (containsKey(key)) {
            return "true".equalsIgnoreCase(get(key).trim());
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns true if the value equals "true". If the value is null, then an
     * UndefinedPropertyException is thrown.
     */
    public boolean getBoolean(final String key) {
        if (containsKey(key)) {
            return "true".equalsIgnoreCase(get(key));
        } else {
            throw new UndefinedPropertyException("Missing required property '" + key
                    + "'");
        }
    }

    /**
     * Returns the long representation of the value. If the value is null, then the default value is
     * returned. If the value isn't a long, then a parse exception will be thrown.
     */
    public long getLong(final String name, final long defaultValue) {
        if (containsKey(name)) {
            return Long.parseLong(get(name));
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the long representation of the value. If the value is null, then a
     * UndefinedPropertyException will be thrown. If the value isn't a long, then a parse exception
     * will be thrown.
     */
    public long getLong(final String name) {
        if (containsKey(name)) {
            return Long.parseLong(get(name));
        } else {
            throw new UndefinedPropertyException("Missing required property '" + name
                    + "'");
        }
    }

    /**
     * Returns the int representation of the value. If the value is null, then the default value is
     * returned. If the value isn't a int, then a parse exception will be thrown.
     */
    public int getInt(final String name, final int defaultValue) {
        if (containsKey(name)) {
            return Integer.parseInt(get(name).trim());
        } else {
            return defaultValue;
        }
    }


    /**
     * Returns the int representation of the value. If the value is null, then a
     * UndefinedPropertyException will be thrown. If the value isn't a int, then a parse exception
     * will be thrown.
     */
    public int getInt(final String name) {
        if (containsKey(name)) {
            return Integer.parseInt(get(name).trim());
        } else {
            throw new UndefinedPropertyException("Missing required property '" + name
                    + "'");
        }
    }

    /**
     * Returns the double representation of the value. If the value is null, then the default value is
     * returned. If the value isn't a double, then a parse exception will be thrown.
     */
    public double getDouble(final String name, final double defaultValue) {
        if (containsKey(name)) {
            return Double.parseDouble(get(name).trim());
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the double representation of the value. If the value is null, then a
     * UndefinedPropertyException will be thrown. If the value isn't a double, then a parse exception
     * will be thrown.
     */
    public double getDouble(final String name) {
        if (containsKey(name)) {
            return Double.parseDouble(get(name).trim());
        } else {
            throw new UndefinedPropertyException("Missing required property '" + name
                    + "'");
        }
    }

    /**
     * Returns the String list representation of the value.
     */
    public List<String> getStringList(final String name, final String sep) {
        final String val = get(name);
        if (val == null || val.trim().length() == 0) {
            return Collections.emptyList();
        }

        if (containsKey(name)) {
            return Arrays.asList(val.split(sep));
        } else {
            throw new UndefinedPropertyException("Missing required property '" + name
                    + "'");
        }
    }

    /**
     * Returns a list of strings with the comma as the separator of the value
     */
    public List<String> getStringList(final String name) {
        return getStringList(name, ",");
    }

    /**
     * Returns a java.util.Properties file populated with the current Properties in here.
     */
    public Properties toProperties() {
        final Properties p = new Properties();
        for (int i = propertiesList.size() - 1; i >= 0; i--) {
            Map<String, String> propertiesMap = propertiesList.get(i);
            for (final String key : propertiesMap.keySet()) {
                p.setProperty(key, get(key));
            }
        }

        return p;
    }

}

