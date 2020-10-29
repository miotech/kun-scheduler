package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miotech.kun.commons.utils.Props;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kun internal used URI which encodes information of a datastore
 * The encoding structure is:
 * <code>
 * &lt;namespace&gt;:&lt;schema&gt;://&lt;hostname&gt;[:port]/&lt;requiredKey1=requiredValue1&gt;,&lt;requiredKey2=requiredValue2&gt;,...[?optionalKey1=optionalValue1[,optionalKey2=optionalValue2...]]
 * </code>
 *
 * @author Josh Ouyang
 */
public class URI implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603945934951L;

    private static final String IPV4_PATTERN = "(?:(?!(?:10\\.|172\\.(?:1[6-9]|2\\d|3[01])\\.|192\\.168\\.).*)" +
            "(?!255\\.255\\.255\\.255)(?:25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|[1-9])" +
            "(?:\\.(?:25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)){3})";

    private static final String VALID_HOSTNAME_PATTERN = "(?:(?:(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*" +
            "(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))";

    private static final Pattern validatePattern =
            Pattern.compile("^([\\w(?:%[0-9a-fA-F]+)]+)" +  // namespace
                    ":([\\w(?:%[0-9a-fA-F]+)]+)" +          // schema
                    "://" +
                    "(" + VALID_HOSTNAME_PATTERN + ")" +          // hostname
                    "(?:\\:([1-6]?[0-9]{0,4}))?" +                                          // port
                    "(?:" +
                    "(?:/((?:[\\w(?:%[0-9a-fA-F]+)]+=[\\w(?:%[0-9a-fA-F]+)]+)(?:,[\\w(?:%[0-9a-fA-F]+)]+=[\\w(?:%[0-9a-fA-F]+)]+)*))?" +  // required properties
                    "(?:\\?" +
                    "((?:[\\w(?:%[0-9a-fA-F]+)]+=[\\w(?:%[0-9a-fA-F]+)]+)(?:,[\\w(?:%[0-9a-fA-F]+)]+=[\\w(?:%[0-9a-fA-F]+)]+)*))?" +  // optional properties
                    ")?$");

    private final String string;

    private final String namespace;

    private final String schema;

    private final String hostName;

    private final Integer port;

    private final Props requiredProps;

    private final Props optionalProps;

    private final boolean isValidIPv4Hostname;

    private URI(String string, String namespace, String schema, String hostName, Integer port) {
        this.string = string;
        this.namespace = namespace;
        this.schema = schema;
        this.hostName = hostName;
        this.port = port;
        this.requiredProps = new Props();
        this.optionalProps = new Props();
        this.isValidIPv4Hostname = Pattern.compile(IPV4_PATTERN).matcher(hostName).matches();
    }

    public static URI from(String uriString) {
        Matcher matcher = validatePattern.matcher(uriString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Invalid argument `uriString` = \"%s\". cannot match required URI pattern.", uriString)
            );
        }
        // else
        String namespace = matcher.group(1);
        String schema = matcher.group(2);
        String hostname = matcher.group(3);
        String portStr = matcher.group(4);
        String requiredPropsStr = matcher.group(5);
        String optionalPropsStr = matcher.group(6);
        Integer port;
        if (StringUtils.isNotBlank(portStr)) {
            port = Integer.valueOf(portStr);
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException(String.format("Invalid port = %s", port));
            }
        } else {
            port = null;
        }
        URI uri = new URI(uriString, namespace, schema, hostname, port);
        if (StringUtils.isNotBlank(requiredPropsStr)) {
            uri.requiredProps.putAll(resolveProps(requiredPropsStr));
        }
        if (StringUtils.isNotBlank(optionalPropsStr)) {
            uri.optionalProps.putAll(resolveProps(optionalPropsStr));
        }
        return uri;
    }

    private static Props resolveProps(String propsString) {
        Props props = new Props();
        String[] kvPairs = propsString.split(",");
        for (String kvPair : kvPairs) {
            String[] pair = kvPair.split("=");
            String key = pair[0];
            String value = pair[1];
            if (props.containsKey(key)) {
                throw new IllegalArgumentException(String.format("Duplicated key in property pair: %s", kvPair));
            }
            // else
            props.put(key, value);
        }
        return props;
    }

    @Override
    public String toString() {
        return this.string;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getHostname() {
        return this.hostName;
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    public Props getRequiredProperties() {
        return this.requiredProps;
    }

    public Props getOptionalProperties() {
        return this.optionalProps;
    }

    public boolean isValidIPv4Hostname() {
        return isValidIPv4Hostname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URI uri = (URI) o;
        return Objects.equals(namespace, uri.namespace) &&
                Objects.equals(schema, uri.schema) &&
                Objects.equals(hostName, uri.hostName) &&
                Objects.equals(port, uri.port) &&
                Objects.equals(requiredProps, uri.requiredProps) &&
                Objects.equals(optionalProps, uri.optionalProps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, schema, hostName, port, requiredProps, optionalProps);
    }
}
