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

    private final PropsProviderList propsProviderList;

    public static Props fromProperties(Properties properties) {
        return new Props(properties);
    }

    public Props() {
        propsProviderList = new PropsProviderList();
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
    public Props(final PropsProvider... providers) {
        this();
        for (int i = providers.length - 1; i >= 0; i--) {
            propsProviderList.add(providers[i]);
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
        for (int i = props.propsProviderList.size() - 1; i >= 0; i--) {
            propsProviderList.add( props.propsProviderList.get(i));
        }
    }

    /**
     * add propsProvider to last
     *
     * @param propsProvider
     */
    public void addPropsProvider(PropsProvider propsProvider) {
        propsProviderList.add(propsProvider);
    }

    /**
     * Put object
     */
    public void put(final String key, final Object value) {
        propsProviderList.put(key, value);

    }

    /**
     * Check key in propertiesMap
     */
    public boolean containsKey(final String key) {
        String trimKey = key.trim();
        for (PropsProvider propsProvider : propsProviderList) {
            String upCaseKey = trimKey.toUpperCase();
            if (propsProvider.containsKey(trimKey) || propsProvider.containsKey(upCaseKey)) {
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
    public String get(final String key) {
        return getValue(key, String.class);
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
            return getValue(key, Boolean.class);
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
            return getValue(key, Boolean.class);
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
            return getValue(name, Long.class);
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
            return getValue(name, Long.class);
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
            return getValue(name, Integer.class);
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
            return getValue(name, Integer.class);
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
            return getValue(name, Double.class);
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
            return getValue(name, Double.class);
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
        for (int i = propsProviderList.size() - 1; i >= 0; i--) {
            PropsProvider provider = propsProviderList.get(i);
            Properties properties = provider.toProperties();
            for (Object key : properties.keySet()) {
                String keyStr = key.toString();
                p.setProperty(keyStr, properties.getProperty(keyStr));
            }

        }
        return p;
    }

    /**
     * Class T should have constructor with @JsonCreator
     * or no args constructor with setter/getter method
     *
     * @param key
     * @param valueType
     * @param <T>
     * @return
     */
    public <T> T getValue(String key, Class<T> valueType) {
        key = key.trim();
        for (int i = 0; i < propsProviderList.size(); i++) {
            PropsProvider propsProvider = propsProviderList.get(i);
            if (propsProvider.containsKey(key)) {
                return propsProvider.getValue(key, valueType);
            }
            String upCaseKey = key.toUpperCase();
            if (propsProvider.containsKey(upCaseKey)) {
                return propsProvider.getValue(upCaseKey, valueType);
            }
        }
        return null;
    }

    /**
     * Class T should have constructor with @JsonCreator
     * or no args constructor with setter/getter method
     *
     * @param key
     * @param valueType
     * @param <T>
     * @return
     */
    public <T> List<T> getValueList(String key, Class<T> valueType) {
        for (int i = 0; i < propsProviderList.size(); i++) {
            PropsProvider propsProvider = propsProviderList.get(i);
            if (propsProvider instanceof JsonProps) {
                JsonProps jsonProps = (JsonProps) propsProvider;
                return jsonProps.getValueList(key, valueType);
            }
        }
        return null;
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Props: ");
        int i = 1;
        for (PropsProvider propsProvider : propsProviderList) {
            result.append("\n provider ").append(i).append(" :").append(propsProvider.toString());
        }
        return result.toString();
    }
}

