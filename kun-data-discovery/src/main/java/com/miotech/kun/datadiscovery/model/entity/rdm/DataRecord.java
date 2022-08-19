

package com.miotech.kun.datadiscovery.model.entity.rdm;

import java.io.Serializable;
import java.util.*;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJson;

/**
 * A CSV record parsed from a CSV file.
 */

public final class DataRecord implements Serializable, Iterable<Object> {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final long serialVersionUID = 1L;

    private Map<String, Integer> mapping;

    private final long recordNumber;

    private final Object[] values;

    public DataRecord(final Object[] values, final Map<String, Integer> mapping, final long recordNumber) {
        this.recordNumber = recordNumber;
        this.values = values != null ? values : EMPTY_STRING_ARRAY;
        this.mapping = mapping;
    }


    public void setMapping(Map<String, Integer> mapping) {
        this.mapping = mapping;
    }

    public Object get(final int i) {
        return values[i];
    }

    public Map<String, Integer> getMapping() {
        return mapping;
    }

    public Object[] getValues() {
        return values;
    }

    public Object get(final String name) {
        if (mapping == null) {
            throw new IllegalStateException(
                    "No header mapping was specified, the record values can't be accessed by name");
        }
        final Integer index = mapping.get(name);
        if (index == null) {
            throw new IllegalArgumentException(String.format("Mapping for %s not found, expected one of %s", name,
                    mapping.keySet()));
        }
        try {
            return values[index.intValue()];
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.format(
                    "Index for header '%s' is %d but CSVRecord only has %d values!", name, index,
                    Integer.valueOf(values.length)));
        }
    }

    public long getRecordNumber() {
        return recordNumber;
    }

    public boolean isMapping() {
        return mapping != null && (!mapping.isEmpty());
    }

    public boolean isConsistent() {
        return isMapping() && mapping.size() == values.length;
    }


    public boolean isMapped(final String name) {
        return mapping != null && mapping.containsKey(name);
    }

    public boolean isSet(final String name) {
        return isMapped(name) && mapping.get(name).intValue() < values.length;
    }

    @Override
    public Iterator<Object> iterator() {
        return toList().iterator();
    }

    public int size() {
        return values.length;
    }

    private List<Object> toList() {
        return Arrays.asList(values);
    }

    @Override
    public String toString() {
        return "CSVRecord [ mapping=" + mapping +
                ", recordNumber=" + recordNumber + ", values=" +
                Arrays.toString(values) + "]";
    }

}
