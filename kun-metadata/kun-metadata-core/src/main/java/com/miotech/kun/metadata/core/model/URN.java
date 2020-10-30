package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Kun internal used URN which encodes information of a datastore
 * The encoding structure is:
 * <code>&lt;protocol&gt;:&lt;hostname&gt;[:&lt;requiredProp1=value1,requiredProp2=value2,...&gt;[:optionalProp1=optionalPropValue1,optionalProp2=optionalPropValue2,...]]</code>
 */
public class URN implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1604023597334L;

    private static final String VALID_HOSTNAME_PATTERN = "(?:(?:(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*" +
            "(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))";

    private static final String PROP_KEY_VALUE_PATTERN = "(?:(?:[a-zA-Z0-9]|(?:%[0-9a-fA-F]+))+)";

    private static final Pattern validatePattern =
            Pattern.compile("^([\\w(?:%[0-9a-fA-F]+)]+)" +    // protocolType
                    ":(" + VALID_HOSTNAME_PATTERN + ")" +     // hostname
                    "(?:" +
                    ":" + "(?:(" + PROP_KEY_VALUE_PATTERN + "=" + PROP_KEY_VALUE_PATTERN + "(?:," + PROP_KEY_VALUE_PATTERN + "=" + PROP_KEY_VALUE_PATTERN + ")*)?)" +  // required properties
                    "(?::(" + PROP_KEY_VALUE_PATTERN + "=" + PROP_KEY_VALUE_PATTERN + "(?:," + PROP_KEY_VALUE_PATTERN + "=" + PROP_KEY_VALUE_PATTERN + ")*))?" +  // optional properties
                    ")?$");

    private transient String urnString = null;

    private final String protocolType;

    private final String hostname;

    private final Props requiredProps;

    private final Props optionalProps;

    private URN(URNBuilder builder) {
        this.hostname = builder.hostname;
        this.protocolType = builder.protocolType;
        this.requiredProps = builder.requiredProps;
        this.optionalProps = builder.optionalProps;
    }

    public static URNBuilder newBuilder() {
        return new URNBuilder();
    }

    public URNBuilder cloneBuilder() {
        return new URNBuilder()
                .withHostname(hostname)
                .withProtocolType(protocolType)
                .withRequiredProps(requiredProps)
                .withOptionalProps(optionalProps);
    }

    public static URN from(String string) {
        if (!validatePattern.matcher(string).matches()) {
            throw new IllegalArgumentException(String.format("Cannot build URN from invalid string: \"%s\"", string));
        }
    }

    @Override
    public String toString() {
        // lazy evaluate and cache the urn string since it is immutable
        if (StringUtils.isBlank(this.urnString)) {
            // if urnString is not initialized
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(encodeToURIEncodedFormat(protocolType));
            stringBuilder.append(":");
            stringBuilder.append(encodeToURIEncodedFormat(hostname));
            stringBuilder.append(":");
            if (!requiredProps.keySet().isEmpty()) {
                stringBuilder.append(propsToEncodedString(requiredProps));
            }
            if (!optionalProps.keySet().isEmpty()) {
                if (requiredProps.keySet().isEmpty()) {
                    stringBuilder.append("::");
                } else {
                    stringBuilder.append(":");
                }
                stringBuilder.append(propsToEncodedString(optionalProps));
            }
            urnString = stringBuilder.toString();
        }
        return urnString;
    }

    private static String encodeToURIEncodedFormat(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(ExceptionUtils.wrapIfChecked(e));
        }
    }

    private static String decodeFromURIEncodedFormat(String str) {
        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(ExceptionUtils.wrapIfChecked(e));
        }
    }

    private static String propsToEncodedString(Props props) {

    }

    public String getProtocolType() {
        return protocolType;
    }

    public String getHostname() {
        return hostname;
    }

    public Props getRequiredProps() {
        return requiredProps;
    }

    public Props getOptionalProps() {
        return optionalProps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URN urn = (URN) o;
        return Objects.equals(protocolType, urn.protocolType) &&
                Objects.equals(hostname, urn.hostname) &&
                Objects.equals(requiredProps, urn.requiredProps) &&
                Objects.equals(optionalProps, urn.optionalProps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolType, hostname, requiredProps, optionalProps);
    }

    public static final class URNBuilder {
        private String protocolType;
        private String hostname;
        private Props requiredProps = new Props();
        private Props optionalProps = new Props();

        private URNBuilder() {
        }

        public URNBuilder withProtocolType(String protocolType) {
            this.protocolType = protocolType;
            return this;
        }

        public URNBuilder withHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public URNBuilder withRequiredProps(Props requiredProps) {
            this.requiredProps = requiredProps;
            return this;
        }

        public URNBuilder withOptionalProps(Props optionalProps) {
            this.optionalProps = optionalProps;
            return this;
        }

        public URN build() {
            return new URN(this);
        }
    }
}
