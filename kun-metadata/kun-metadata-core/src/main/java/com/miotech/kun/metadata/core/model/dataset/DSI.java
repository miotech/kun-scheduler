package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <b>Data Store Identifier</b> (DSI) class encodes information of a datastore
 * The encoding structure is:
 * <code>&lt;protocol&gt;[:&lt;props1=value1,props2=value2,...&gt;[:extra1=extraValue1,extra2=extraValue2,...]]</code>
 *
 * @author Josh Ouyang
 */
public class DSI implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1604023597334L;

    private static final String PROP_KEY_VALUE_PATTERN = "(?:(?:[a-zA-Z0-9_]|[^\\w,:%]|(?:%[0-9a-fA-F]+))+)";

    private static final Pattern validatePattern =
            Pattern.compile("^([a-zA-Z0-9_]+)" +    // store type
                    "(?:" +
                    ":" + "(?:(" + PROP_KEY_VALUE_PATTERN + "=" + PROP_KEY_VALUE_PATTERN + "(?:," + PROP_KEY_VALUE_PATTERN + "=" + PROP_KEY_VALUE_PATTERN + ")*)?)" +  // properties
                    "(?::(" + PROP_KEY_VALUE_PATTERN + "=" + PROP_KEY_VALUE_PATTERN + "(?:," + PROP_KEY_VALUE_PATTERN + "=" + PROP_KEY_VALUE_PATTERN + ")*))?" +  // extras
                    ")?$");

    private transient String dsiString;

    private transient String essentialDsiString;

    private final String storeType;

    private final ImmutableMap<String, String> props;

    private final ImmutableMap<String, String> extras;

    public String getStoreType() {
        return storeType;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    private DSI(DSIBuilder builder) {
        Preconditions.checkNotNull(builder);
        Preconditions.checkArgument(StringUtils.isNotBlank(builder.storeType), "storeType cannot be blank when building dsi");

        this.storeType = builder.storeType;
        this.props = ImmutableMap.copyOf(builder.props);
        this.extras = ImmutableMap.copyOf(builder.extras);
    }

    public static DSIBuilder newBuilder() {
        return new DSIBuilder();
    }

    public DSIBuilder cloneBuilder() {
        DSIBuilder builder = new DSIBuilder();
        builder.withStoreType(this.storeType);
        for (Map.Entry<String, String> entry : this.props.entrySet()) {
            builder.putProperty(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : this.extras.entrySet()) {
            builder.putExtra(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    public static DSI from(String string) {
        Matcher matcher = validatePattern.matcher(string);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Cannot build DSI from invalid string: \"%s\"", string));
        }
        String storeType = matcher.group(1);
        String propsStr = matcher.group(2);
        String extraStr = matcher.group(3);

        Map<String, String> propsMap = transformEncodedStringToMap(propsStr);
        Map<String, String> extraMap = transformEncodedStringToMap(extraStr);

        return new DSIBuilder()
                .withStoreType(storeType)
                .withProps(propsMap)
                .withExtras(extraMap)
                .build();
    }

    private static Map<String, String> transformEncodedStringToMap(String encodedString) {
        Map<String, String> props = new HashMap<>();
        if (StringUtils.isBlank(encodedString)) {
            return props;
        }
        String[] kvPairs = encodedString.split(",");
        for (String kvPair : kvPairs) {
            String[] pair = kvPair.split("=");
            String key = pair[0];
            String value = pair[1];
            if (props.containsKey(key)) {
                throw new IllegalArgumentException(String.format("Duplicated key in property pair: %s", kvPair));
            }
            // else
            props.put(decodeString(key), decodeString(value));
        }
        return props;
    }

    /**
     * Decodes string from {@code application/x-www-form-urlencoded}
     * format using a specific encoding scheme.
     * @param str string in format of application/x-www-form-urlencoded
     * @return decoded string
     */
    private static String decodeString(String str) {
        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(ExceptionUtils.wrapIfChecked(e));
        }
    }

    /**
     * Convert DSI to URL-encoded format string
     * @return encoded DSI string
     */
    @Override
    public String toString() {
        // lazy evaluate and cache the urn string since it is immutable
        if (StringUtils.isBlank(this.dsiString)) {
            // if dsiString is not initialized
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(encodeString(storeType));
            if (!props.keySet().isEmpty()) {
                stringBuilder.append(":");
                stringBuilder.append(mapToEncodedString(props));
            }
            if (!extras.keySet().isEmpty()) {
                if (props.keySet().isEmpty()) {
                    stringBuilder.append("::");
                } else {
                    stringBuilder.append(":");
                }
                stringBuilder.append(mapToEncodedString(extras));
            }
            dsiString = stringBuilder.toString();
        }
        return dsiString;
    }

    /**
     * Convert DSI to URL-encoded format string but only reserves props part (extras will not be included)
     * @return encoded DSI string without extra parts
     */
    public String toEssentialString() {
        // lazy evaluate and cache the urn string since it is immutable
        if (StringUtils.isBlank(this.essentialDsiString)) {
            // if dsiString is not initialized
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(encodeString(storeType));
            if (!props.keySet().isEmpty()) {
                stringBuilder.append(":");
                stringBuilder.append(mapToEncodedString(props));
            }
            essentialDsiString = stringBuilder.toString();
        }
        return essentialDsiString;
    }

    /**
     * Convert DSI to URL-encoded format string
     * @return encoded DSI string
     */
    public String toFullString() {
        return this.toString();
    }

    /**
     * Translates a string into {@code application/x-www-form-urlencoded}
     * format using a specific encoding scheme.
     * @param str string in raw format
     * @return encoded string in application/x-www-form-urlencoded scheme
     */
    private static String encodeString(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(ExceptionUtils.wrapIfChecked(e));
        }
    }

    /**
     * converts properties/extras map to encoded form url scheme string.
     * @param props key-value map
     * @return encoded map string
     */
    private static String mapToEncodedString(Map<String, String> props) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> sortedKeyList = props.keySet().stream().sorted().collect(Collectors.toList());
        for (String key : sortedKeyList) {
            stringBuilder
                    .append(encodeString(key))
                    .append('=')
                    .append(encodeString(props.get(key)))
                    .append(',');
        }
        // remove last comma
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DSI dsi = (DSI) o;
        return Objects.equals(storeType, dsi.storeType) &&
                Objects.equals(props, dsi.props) &&
                Objects.equals(extras, dsi.extras);
    }

    /**
     * Is it the same datastore as the other DSI represents?
     * @param dsi the comparing target
     * @return compare result
     */
    public boolean sameStoreAs(DSI dsi) {
        return Objects.equals(storeType, dsi.storeType) &&
                Objects.equals(props, dsi.props);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeType, props, extras);
    }

    public static final class DSIBuilder {
        private String storeType;
        private Map<String, String> props = new HashMap<>();
        private Map<String, String> extras = new HashMap<>();

        private DSIBuilder() {
        }

        public DSIBuilder withStoreType(String storeType) {
            this.storeType = storeType;
            return this;
        }

        public DSIBuilder withProps(Map<String, String> props) {
            this.props = props;
            return this;
        }

        public DSIBuilder withExtras(Map<String, String> extras) {
            this.extras = extras;
            return this;
        }

        public DSIBuilder putProperty(String key, String value) {
            this.props.put(key, value);
            return this;
        }

        public DSIBuilder putExtra(String key, String value) {
            this.extras.put(key, value);
            return this;
        }

        public DSI build() {
            return new DSI(this);
        }
    }
}
