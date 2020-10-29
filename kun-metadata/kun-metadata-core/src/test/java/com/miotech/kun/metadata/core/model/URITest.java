package com.miotech.kun.metadata.core.model;

import com.miotech.kun.commons.utils.Props;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class URITest {
    @Test
    public void URI_createFromValidURLString_shouldWork() {
        // Prepare
        String uri1String = "miotech:hdfs://127.0.0.1";
        String uri2String = "miotech:postgres://db-server:5432/db=test,schema=dm,table=company";
        String uri3String = "myNameSpace:mongodb://subdomain.db-server/db=docs,collection=abc?username=root,password=example123";
        String uri4String = "myNameSpace:mongodb://subdomain.db-server?auth=true";

        // Validate URI 1
        URI uri1 = URI.from(uri1String);
        assertThat(uri1.getNamespace(), is("miotech"));
        assertThat(uri1.getSchema(), is("hdfs"));
        assertThat(uri1.getHostname(), is("127.0.0.1"));
        assertThat(uri1.getPort(), is(Optional.empty()));
        assertThat(uri1.toString(), is(uri1String));
        assertThat(uri1.isValidIPv4Hostname(), is(true));

        // Validate URI 2
        URI uri2 = URI.from(uri2String);
        assertThat(uri2.getNamespace(), is("miotech"));
        assertThat(uri2.getSchema(), is("postgres"));
        assertThat(uri2.getHostname(), is("db-server"));
        assertThat(uri2.getPort().get(), is(5432));
        assertThat(uri2.toString(), is(uri2String));
        assertThat(uri2.isValidIPv4Hostname(), is(false));

        Props requiredPropsOfUri2 = uri2.getRequiredProperties();
        assertThat(requiredPropsOfUri2.keySet().size(), is(3));
        assertThat(requiredPropsOfUri2.getString("db"), is("test"));
        assertThat(requiredPropsOfUri2.getString("schema"), is("dm"));
        assertThat(requiredPropsOfUri2.getString("table"), is("company"));

        assertThat(uri2.getOptionalProperties().keySet().size(), is(0));

        // Validate URI 3
        URI uri3 = URI.from(uri3String);
        assertThat(uri3.getNamespace(), is("myNameSpace"));
        assertThat(uri3.getSchema(), is("mongodb"));
        assertThat(uri3.getHostname(), is("subdomain.db-server"));
        assertThat(uri3.getPort(), is(Optional.empty()));
        assertThat(uri3.toString(), is(uri3String));
        assertThat(uri3.isValidIPv4Hostname(), is(false));

        Props requiredPropsOfUri3 = uri3.getRequiredProperties();
        Props optionalPropsOfUri3 = uri3.getOptionalProperties();
        assertThat(requiredPropsOfUri3.keySet().size(), is(2));
        assertThat(requiredPropsOfUri3.getString("db"), is("docs"));
        assertThat(requiredPropsOfUri3.getString("collection"), is("abc"));
        assertThat(optionalPropsOfUri3.keySet().size(), is(2));
        assertThat(optionalPropsOfUri3.getString("username"), is("root"));
        assertThat(optionalPropsOfUri3.getString("password"), is("example123"));

        // Validate URI 4
        URI uri4 = URI.from(uri4String);
        Props requiredPropsOfUri4 = uri4.getRequiredProperties();
        Props optionalPropsOfUri4 = uri4.getOptionalProperties();
        assertThat(requiredPropsOfUri4.keySet().size(), is(0));
        assertThat(optionalPropsOfUri4.keySet().size(), is(1));
        assertThat(optionalPropsOfUri4.getString("auth"), is("true"));
        assertThat(optionalPropsOfUri4.getBoolean("auth"), is(true));
    }

    @Test
    public void URI_createFromInvalidURLString_shouldThrowException() {
        // Prepare
        String uriWithoutSchema = "miotech:://127.0.0.1";
        String uriWithoutNamespace = "postgres://127.0.0.1";
        String uriWithInvalidPort = "miotech:postgres://127.0.0.1:69999";
        String[] urisWithInvalidPropsPattern = {
                "miotech:postgres://db-server:5432/db=",
                "miotech:postgres://db-server:5432/db=123,,",
                "miotech:postgres://db-server:5432/db=a,db=b",
                "miotech:postgres://db-server:5432/=",
                "miotech:postgres://db-server:5432/=,=",
                "miotech:postgres://db-server:5432/==",
                "miotech:postgres://db-server:5432/,",
                "miotech:postgres://db-server:5432/?,",
                "miotech:postgres://db-server:5432/?a=,",
                "miotech:postgres://db-server:5432/?=b"
        };

        // Validate
        assertThrowsIllegalArgumentExceptionWhenBuildingURI(uriWithoutSchema);
        assertThrowsIllegalArgumentExceptionWhenBuildingURI(uriWithoutNamespace);
        assertThrowsIllegalArgumentExceptionWhenBuildingURI(uriWithInvalidPort);
        for (String invalidPattern : urisWithInvalidPropsPattern) {
            assertThrowsIllegalArgumentExceptionWhenBuildingURI(invalidPattern);
        }
    }

    private void assertThrowsIllegalArgumentExceptionWhenBuildingURI(String uriString) {
        try {
            URI.from(uriString);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void URI_equality_test() {
        // URIs built from the same uri should always be equivalent
        String uriSample = "miotech:mysql://192.168.1.102:3306/db=test,table=employees";

        URI uri1 = URI.from(uriSample);
        URI uri2 = URI.from(uriSample);
        // Validate reflexivity
        assertEquals(uri1, uri1);
        assertEquals(uri2, uri2);
        // Validate equivalency
        assertEquals(uri1, uri2);
    }
}
