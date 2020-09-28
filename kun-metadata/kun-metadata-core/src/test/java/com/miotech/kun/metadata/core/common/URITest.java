package com.miotech.kun.metadata.core.common;

import org.dmfs.rfc3986.Authority;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class URITest {
    @Test
    public void URI_createFromValidURIString_shouldCreateInstanceProperly() {
        URI uri = URI.from("https://127.0.0.1:80/path/to/anything?query=123");
        URI jdbcUri = URI.from("mysql://dbhost/testdb");
        URI urn = URI.from("urn:example:animal:ferret:nose");
        assertEquals("https://127.0.0.1:80/path/to/anything?query=123", uri.toString());
        assertEquals("mysql://dbhost/testdb", jdbcUri.toString());
        assertEquals("urn:example:animal:ferret:nose", urn.toString());
    }

    @Test
    public void URI_createFromInvalidURIString_shouldThrowException() {
        try {
            URI.from("s:/This is a random string");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void URI_extractScheme_shouldWorkProperly() {
        URI uriWithScheme = URI.from("mongodb://127.0.0.1:33060/test");
        URI uriWithoutScheme = URI.from("miotech.com");

        Optional<String> scheme1 = uriWithScheme.getScheme();
        Optional<String> scheme2 = uriWithoutScheme.getScheme();

        assertEquals("mongodb", scheme1.get());
        assertFalse(scheme2.isPresent());
    }

    @Test
    public void URI_extractAuthority_shouldWorkProperly() {
        URI uriWithAuthority = URI.from("mongodb://127.0.0.1:33060/test");
        URI uriWithUserInfoAuthority = URI.from("ssh://root:pwd123@127.0.0.1:33060/test");
        URI uriWithoutAuthority = URI.from("urn:example:animal:ferret:nose");

        Optional<Authority> authority = uriWithAuthority.getAuthority();
        Optional<Authority> authorityWithUserInfo = uriWithUserInfoAuthority.getAuthority();
        Optional<Authority> authorityEmpty = uriWithoutAuthority.getAuthority();

        assertEquals("127.0.0.1", authority.get().host().toString());
        assertEquals(33060, authority.get().port().value().intValue());
        assertEquals("root:pwd123", authorityWithUserInfo.get().userInfo().value().toString());
        assertFalse(authorityEmpty.isPresent());
    }

    @Test
    public void URI_extractPath_shouldWorkProperly() {
        URI uriWithPath = URI.from("mongodb://127.0.0.1:33060/test");
        URI uriWithoutPath = URI.from("ssh://127.0.0.1");
        URI urn = URI.from("urn:isbn:0451450523");

        assertEquals("/test", uriWithPath.getPath());
        assertEquals("", uriWithoutPath.getPath());
        assertEquals(":isbn:0451450523", urn.getPath());
    }

    @Test
    public void URI_equality_shouldWorkProperly() {
        URI uri1 = URI.from("mongodb://127.0.0.1:33060/test");
        URI uri1Equal = URI.from("mongodb://127.0.0.1:33060/test");
        assertEquals(uri1, uri1Equal);

        // relative path should be normalized
        URI uri2 = URI.from("http://www.miotech.com/path/dir1/dir2/a.txt");
        URI uri2Equal = URI.from("http://www.miotech.com/path/dir1/dir2/dir3/../a.txt");
        assertEquals(uri2, uri2Equal);

        // Falsy cases
        URI uri3 = URI.from("http://www.miotech.com/path/dir1/dir2/a.txt");
        URI uri4 = URI.from("http://www.miotech.com/path/dir1/dir2/b.txt");
        assertNotEquals(uri3, uri4);
        assertNotEquals(uri3, new Object());
    }

    @Test
    public void URI_shouldCompatibleWithJDBCUri() {
       String jdbcUriStr = "jdbc:postgresql://hostname/dbname?user=username&password=password";
       URI uri = URI.from(jdbcUriStr);
       assertEquals(jdbcUriStr, uri.toString());
    }

    @Test
    public void URI_shouldCompatibleWithNoProtocolPrefix() {
        String emptyProtocolStr1 = "hostname";
        URI uri1 = URI.from(emptyProtocolStr1);
        assertEquals(emptyProtocolStr1, uri1.toString());

        String emptyProtocolStr2 = "hostname/a/b/c";
        URI uri2 = URI.from(emptyProtocolStr2);
        assertEquals(emptyProtocolStr2, uri2.toString());
    }
}
