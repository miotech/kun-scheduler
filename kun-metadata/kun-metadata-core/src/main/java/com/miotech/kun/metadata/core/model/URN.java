package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miotech.kun.commons.utils.Props;

import java.io.Serializable;
import java.util.Objects;

/**
 * Kun internal used URN which encodes information of a datastore
 * The encoding structure is:
 * <code>urn:&lt;protocol&gt;:&lt;hostname&gt;[:&lt;requiredProp1=value1,requiredProp2=value2,...&gt;[:optionalProp1=optionalPropValue1,optionalProp2=optionalPropValue2,...]]</code>
 */
public class URN implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1604023597334L;

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
        // TODO
    }

    @Override
    public String toString() {
        // TODO
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
