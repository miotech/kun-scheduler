package com.miotech.kun.metadata.core.model;

import com.miotech.kun.metadata.core.model.dataset.DSI;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DSITest {
    @Test
    public void DSI_buildFromValidString_shouldCreateProperly() {
        // Prepare
        String dsiString1 = "mysql:host=db-server,port=3306,db=test,table=company";
        String dsiString2 = "postgres:host=192.168.1.2,port=5432,db=test,schema=dm,table=company";
        String dsiString3 = "file:path=/data/bin/abc.bin:stream=true,stderr=/logs/error.log";
        String dsiString4 = "ssh:host=192.168.1.5";
        String dsiString5 = "randomType::abc=def";

        // Process
        DSI dsi1 = DSI.from(dsiString1);
        DSI dsi2 = DSI.from(dsiString2);
        DSI dsi3 = DSI.from(dsiString3);
        DSI dsi4 = DSI.from(dsiString4);
        DSI dsi5 = DSI.from(dsiString5);

        // Validate
        assertThat(dsi1.getStoreType(), is("mysql"));
        assertThat(dsi2.getStoreType(), is("postgres"));
        assertThat(dsi3.getStoreType(), is("file"));
        assertThat(dsi4.getStoreType(), is("ssh"));
        assertThat(dsi5.getStoreType(), is("randomType"));

        assertThat(dsi1.getProps().size(), is(4));
        assertThat(dsi1.getExtras().size(), is(0));
        assertThat(dsi1.getProps().get("host"), is("db-server"));
        assertThat(dsi1.getProps().get("port"), is("3306"));
        assertThat(dsi1.getProps().get("db"), is("test"));
        assertThat(dsi1.getProps().get("table"), is("company"));

        assertThat(dsi2.getProps().size(), is(5));
        assertThat(dsi2.getExtras().size(), is(0));
        assertThat(dsi2.getProps().get("host"), is("192.168.1.2"));
        assertThat(dsi2.getProps().get("port"), is("5432"));
        assertThat(dsi2.getProps().get("db"), is("test"));
        assertThat(dsi2.getProps().get("schema"), is("dm"));
        assertThat(dsi2.getProps().get("table"), is("company"));

        assertThat(dsi3.getProps().size(), is(1));
        assertThat(dsi3.getExtras().size(), is(2));
        assertThat(dsi3.getProps().get("path"), is("/data/bin/abc.bin"));
        assertThat(dsi3.getExtras().get("stream"), is("true"));
        assertThat(dsi3.getExtras().get("stderr"), is("/logs/error.log"));

        assertThat(dsi4.getProps().size(), is(1));
        assertThat(dsi4.getExtras().size(), is(0));
        assertThat(dsi4.getProps().get("host"), is("192.168.1.5"));

        assertThat(dsi5.getProps().size(), is(0));
        assertThat(dsi5.getExtras().size(), is(1));
        assertThat(dsi5.getExtras().get("abc"), is("def"));

        // when reverse convert to string, dsi should sort property keys in lexicographical order,
        // and special characters should be encoded properly.
        assertThat(dsi1.toString(), is("mysql:db=test,host=db-server,port=3306,table=company:"));
        assertThat(dsi2.toString(), is("postgres:db=test,host=192.168.1.2,port=5432,schema=dm,table=company:"));
        assertThat(dsi3.toString(), is("file:path=%2Fdata%2Fbin%2Fabc.bin:stderr=%2Flogs%2Ferror.log,stream=true"));
        assertThat(dsi4.toString(), is("ssh:host=192.168.1.5:"));
        assertThat(dsi5.toString(), is("randomType::abc=def"));
    }

    @Test
    public void DSI_buildFromStringWithURIEncodedCharacters_shouldParseProperly() {
        // Prepare
        String dsiStringEncoded1 = "https:host=localhost,key1=value+1,key2=value%40%21%242,key3=value%253%2c4";
        String dsiStringEncoded2 = "ssh:host=%3a%3affff%3a0.0.0.0";

        // Process
        DSI dsi1 = DSI.from(dsiStringEncoded1);
        DSI dsi2 = DSI.from(dsiStringEncoded2);

        // Validate
        Map<String, String> dsi1Props = dsi1.getProps();
        assertThat(dsi1Props.get("host"), is("localhost"));
        assertThat(dsi1Props.get("key1"), is("value 1"));
        assertThat(dsi1Props.get("key2"), is("value@!$2"));
        assertThat(dsi1Props.get("key3"), is("value%3,4"));

        assertThat(dsi2.getProps().get("host"), is("::ffff:0.0.0.0"));

        // when convert to string, special characters should be encoded
        assertThat(dsi1.toString(), is("https:host=localhost,key1=value+1,key2=value%40%21%242,key3=value%253%2C4:"));
        assertThat(dsi2.toString(), is("ssh:host=%3A%3Affff%3A0.0.0.0:"));
    }

    @Test
    public void DSI_buildFromBuilder_shouldEquivalentToBuildFromString() {
        // Prepare
        DSI.DSIBuilder builder1 = DSI.newBuilder().withStoreType("mongodb");
        builder1.putProperty("host", "127.0.0.1");
        builder1.putProperty("port", "27017");
        builder1.putProperty("db", "test");
        builder1.putProperty("collection", "company");
        DSI dsi1 = builder1.build();

        DSI.DSIBuilder builder2 = DSI.newBuilder().withStoreType("file");
        builder2.putExtra("name", "\"foobar.txt\"");
        DSI dsi2 = builder2.build();

        DSI dsiFromString1 = DSI.from("mongodb:host=127.0.0.1,port=27017,db=test,collection=company");
        DSI dsiFromString2 = DSI.from("file::name=\"foobar.txt\"");

        // Validate
        assertEquals(dsi1, dsiFromString1);
        assertTrue(dsi1.sameStoreAs(dsiFromString1));
        assertEquals("mongodb:collection=company,db=test,host=127.0.0.1,port=27017:", dsiFromString1.toString());
        assertEquals(dsi1.toString(), dsiFromString1.toString());

        assertEquals(dsi2, dsiFromString2);
        assertTrue(dsi2.sameStoreAs(dsiFromString2));
        assertEquals("file::name=%22foobar.txt%22", dsiFromString2.toString());
        assertEquals(dsi2.toString(), dsiFromString2.toString());
    }

    @Test
    public void DSI_buildFromInvalidString_shouldThrowException() {
        // Prepare
        String[] dsiStringsWithInvalidPattern = {
                ":host=127.0.0.1",    // no store type
                "host=127.0.0.1",     // no store type
                "postgres:host=db-server,port=5432,db=123,",          // invalid comma at tail
                "postgres:host=db-server,port=5432,db=test,db=test",  // Duplicated key
                "postgres:host=db-server:",
                "postgres:=",
                "postgres:==",
                "postgres:=,",
                "postgres:,,",
                "postgres:a=b:=",
                "postgres:a=c:=b",
                "postgres:a=b:c=d:",
                "postgres:a=b:c=d:e=f",
                "postgres:a=%G",
        };
        for (String invalidPattern : dsiStringsWithInvalidPattern) {
            assertThrowsIllegalArgumentExceptionWhenBuildingURI(invalidPattern);
        }
    }

    private void assertThrowsIllegalArgumentExceptionWhenBuildingURI(String uriString) {
        try {
            DSI.from(uriString);
            System.err.printf("FAILED TO DETECT INVALID STRING: \"%s\"", uriString);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void cloneDSI_shouldWorkProperly() {
        // Prepare
        DSI originalDSI = DSI.from("mysql:host=localhost,port=5432:user=root,password=1234");

        // Process
        DSI unmodifiedDSI = originalDSI.cloneBuilder().build();
        DSI modifiedDSI = originalDSI.cloneBuilder()
                .putProperty("host", "127.0.0.1")
                .putExtra("password", "he,llo@2020")
                .build();

        DSI modifiedDSIWithSameProperties = originalDSI.cloneBuilder()
                .putExtra("password", "he,llo@2020")
                .build();

        // Validate
        assertEquals(originalDSI, unmodifiedDSI);
        assertEquals(originalDSI.toString(), unmodifiedDSI.toString());
        assertTrue(originalDSI.sameStoreAs(unmodifiedDSI));

        assertEquals("mysql:host=127.0.0.1,port=5432:password=he%2Cllo%402020,user=root", modifiedDSI.toString());
        assertFalse(modifiedDSI.sameStoreAs(originalDSI));

        assertEquals("mysql:host=localhost,port=5432:password=he%2Cllo%402020,user=root", modifiedDSIWithSameProperties.toString());
        assertTrue(modifiedDSIWithSameProperties.sameStoreAs(originalDSI));
    }
}
