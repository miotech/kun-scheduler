package com.miotech.kun.metadata.core.common;

import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.core.utils.URIValidator;
import org.dmfs.rfc3986.Authority;
import org.dmfs.rfc3986.Uri;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;
import org.dmfs.rfc3986.uris.Normalized;
import org.dmfs.rfc3986.uris.Text;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Josh Ouyang
 * Customized URI implementation compatible to RFC 3986.
 * See <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986 spec</a> for details
 *
 * The following are two example URIs and their component parts:
 *
 *          foo://example.com:8042/over/there?name=ferret#nose
 *          \_/   \______________/\_________/ \_________/ \__/
 *           |           |            |            |        |
 *        scheme     authority       path        query   fragment
 *           |   _____________________|__
 *          / \ /                        \
 *          urn:example:animal:ferret:nose
 */
public class URI implements Serializable, Comparable<Object> {

    private static final Long serialVersionUID = -202008209738796512L;

    /**
     * Internal representation of a URI compatible to RFC 3986
     */
    private transient Uri internalUri;

    private volatile String string;

    private volatile boolean isJdbcUri;

    private URI() {
    }

    /**
     * @param uriString
     * @return
     */
    public static URI from(String uriString) {
        Preconditions.checkArgument(URIValidator.isValid(uriString), "Invalid URI string: %s", uriString);
        URI instance = new URI();
        instance.isJdbcUri = uriString.startsWith("jdbc:");
        String codedStr = uriString;
        if (instance.isJdbcUri) {
            codedStr = codedStr.replaceFirst("jdbc:", "");
        }
        instance.internalUri = new Normalized(new LazyUri(new Precoded(codedStr)));
        instance.string = (new Text(instance.internalUri)).toString();
        return instance;
    }

    @Override
    public String toString() {
        if (isJdbcUri) {
            return "jdbc:" + string;
        }
        return string;
    }

    /**
     * Get optional scheme.
     * @return An optional scheme
     */
    public Optional<String> getScheme() {
        if (internalUri.scheme().isPresent()) {
            return Optional.of(internalUri.scheme().value().toString());
        }
        // else
        return Optional.empty();
    }

    /**
     * Get optional authority. An authority has a host, optional user info and an optional port.
     * @return An optional authority
     */
    public Optional<Authority> getAuthority() {
        if (internalUri.authority().isPresent()) {
            return Optional.of(internalUri.authority().value());
        }
        // else
        return Optional.empty();
    }

    /**
     * Returns the path of this URI reference.
     * @return A string of path. The path is always present, but may be empty.
     */
    public String getPath() {
        if (internalUri.path().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Optional<String> schemeOptional = getScheme();
        if (schemeOptional.isPresent() && schemeOptional.get().equals("urn")) {
            internalUri.path().forEach(p -> {
                sb.append(":");
                sb.append(p.toString());
            });
        } else {
            internalUri.path().forEach(p -> {
                if (!p.toString().isEmpty()) {
                    sb.append("/");
                    sb.append(p.toString());
                }
            });
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof URI) {
            return Objects.equals(this.toString(), obj.toString());
        }
        // else
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }
}
