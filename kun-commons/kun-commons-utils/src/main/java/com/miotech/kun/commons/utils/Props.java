package com.miotech.kun.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Hashmap implementation of a hierarchical properties with helpful converter functions and
 * Exception throwing. This class is not threadsafe.
 */
public class Props {

    private static final Logger logger = LoggerFactory.getLogger(Props.class);

    private final Map<String, String> propertiesMap;

    private String source = null;

    public static Props fromProperties(Properties properties){
        return new Props(properties);
    }

    public Props() {
        propertiesMap = new HashMap<>();
    }

    public Props(final Properties... properties) {
        this();
        for (int i = properties.length - 1; i >= 0; i--) {
            this.put(properties[i]);
        }
    }

    /**
     * Create props from property input streams
     */
    public Props(final InputStream inputStream) throws IOException {
        this();
        loadFrom(inputStream);
    }

    /**
     * Create properties from maps of properties
     */
    public Props(final Map<String, String>... props) {
        this();
        for (int i = props.length - 1; i >= 0; i--) {
            this.putAll(props[i]);
        }
    }

    private void loadFrom(final InputStream inputStream) throws IOException {
        final Properties properties = new Properties();
        properties.load(inputStream);
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
            this.propertiesMap.put(propName, properties.getProperty(propName));
        }
    }

    /**
     * Put string
     */
    public String put(final String key, final String value) {
        return this.propertiesMap.put(key, value);
    }

    /**
     * Put boolean
     */
    public String put(final String key, final Boolean value) {
        return this.propertiesMap.put(key, value.toString());
    }

    /**
     * Put integer
     */
    public String put(final String key, final Integer value) {
        return this.propertiesMap.put(key, value.toString());
    }

    /**
     * Put Long. Stores as String.
     */
    public String put(final String key, final Long value) {
        return this.propertiesMap.put(key, value.toString());
    }

    /**
     * Put Double. Stores as String.
     */
    public String put(final String key, final Double value) {
        return this.propertiesMap.put(key, value.toString());
    }

    /**
     * Put all properties in the props into the current props. Will handle null p.
     */
    public void putAll(final Props p) {
        if (p == null) {
            return;
        }
        for (final String key : p.getKeySet()) {
            this.put(key, p.get(key));
        }
    }

    /**
     * Put everything in the map into the props.
     */
    public void putAll(final Map<? extends String, ? extends String> m) {
        if (m == null) {
            return;
        }

        for (final Map.Entry<? extends String, ? extends String> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns a set of all keys, including the parents
     */
    public Set<String> getKeySet() {
        final HashSet<String> keySet = new HashSet<>();
        keySet.addAll(keySet());
        return keySet;
    }

    /**
     * Get the key set from the Props
     */
    public Set<String> keySet() {
        return this.propertiesMap.keySet();
    }

    /**
     * Check key in propertiesMap
     */
    public boolean containsKey(final Object k) {
        return this.propertiesMap.containsKey(k);
    }

    /**
     * Check value in propertiesMap
     */
    public boolean containsValue(final Object value) {
        return this.propertiesMap.containsValue(value);
    }

    /**
     * Return value if available in propertiesMap
     *
     * @return
     */
    public String get(final Object key) {
        if (this.propertiesMap.containsKey(key)) {
            return this.propertiesMap.get(key);
        } else {
            return null;
        }
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
     * Clones the Props p object.
     */
    public static Props clone(final Props p) {
        final Props dest = new Props();
        for (final String key : p.keySet()) {
            dest.put(key, p.get(key));
        }
        return dest;
    }

    /**
     * Store only those properties defined at this local level
     *
     * @param file The file to write to
     * @throws IOException If the file can't be found or there is an io error
     */
    public void store(final File file) throws IOException {
        final BufferedOutputStream out =
                new BufferedOutputStream(new FileOutputStream(file));
        try {
            store(out);
        } finally {
            out.close();
        }
    }

    /**
     * Store only those properties defined at this local level
     *
     * @param out The output stream to write to
     * @throws IOException If the file can't be found or there is an io error
     */
    public void store(final OutputStream out) throws IOException {
        final Properties p = new Properties();
        for (final String key : this.propertiesMap.keySet()) {
            p.setProperty(key, get(key));
        }
        p.store(out, null);
    }

    /**
     * Returns a java.util.Properties file populated with the current Properties in here.
     */
    public Properties toProperties() {
        final Properties p = new Properties();
        for (final String key : this.propertiesMap.keySet()) {
            p.setProperty(key, get(key));
        }

        return p;
    }

    /**
     * Get Source information
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Set Source information
     */
    public Props setSource(final String source) {
        this.source = source;
        return this;
    }

    /**
     * override object's default toString function
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("{");
        for (final Map.Entry<String, String> entry : this.propertiesMap.entrySet()) {
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
            builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }
}

