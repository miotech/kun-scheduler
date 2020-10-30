package com.miotech.kun.metadata.core.model;

import com.miotech.kun.commons.utils.Props;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class URNTest {
    @Test
    public void URN_buildFromValidString_shouldCreateProperly() {
        // Prepare
        String urnString1 = "mysql:localhost:port=3306,db=test,table=company";
        String urnString2 = "postgres:192.168.1.2:port=5432,db=test,schema=dm,table=company";
        String urnString3 = "file:127.0.0.1:path=/data/bin/abc.bin:stream=true,stderr=/logs/error.log";
        String urnString4 = "ssh:192.168.1.5";
        String urnString5 = "ssh:192.168.1.5::abc=def";

        // Process
        URN urn1 = URN.from(urnString1);
        URN urn2 = URN.from(urnString2);
        URN urn3 = URN.from(urnString3);
        URN urn4 = URN.from(urnString4);
        URN urn5 = URN.from(urnString5);

        // Validate
        assertThat(urn1.getProtocolType(), is("mysql"));
        assertThat(urn2.getProtocolType(), is("postgres"));
        assertThat(urn3.getProtocolType(), is("file"));
        assertThat(urn4.getProtocolType(), is("ssh"));
        assertThat(urn5.getProtocolType(), is("ssh"));

        assertThat(urn1.getHostname(), is("localhost"));
        assertThat(urn2.getHostname(), is("192.168.1.2"));
        assertThat(urn3.getHostname(), is("127.0.0.1"));
        assertThat(urn4.getHostname(), is("192.168.1.5"));
        assertThat(urn5.getHostname(), is("192.168.1.5"));

        assertThat(urn1.getRequiredProps().keySet().size(), is(3));
        assertThat(urn1.getOptionalProps().keySet().size(), is(0));
        assertThat(urn1.getRequiredProps().getInt("port"), is(3306));
        assertThat(urn1.getRequiredProps().getString("db"), is("test"));
        assertThat(urn1.getRequiredProps().getString("table"), is("company"));

        assertThat(urn2.getRequiredProps().keySet().size(), is(4));
        assertThat(urn2.getOptionalProps().keySet().size(), is(0));
        assertThat(urn2.getRequiredProps().getInt("port"), is(5432));
        assertThat(urn2.getRequiredProps().getString("db"), is("test"));
        assertThat(urn2.getRequiredProps().getString("schema"), is("dm"));
        assertThat(urn2.getRequiredProps().getString("table"), is("company"));

        assertThat(urn3.getRequiredProps().keySet().size(), is(1));
        assertThat(urn3.getOptionalProps().keySet().size(), is(2));
        assertThat(urn3.getRequiredProps().getString("path"), is("/data/bin/abc.bin"));
        assertThat(urn3.getOptionalProps().getBoolean("stream"), is(true));
        assertThat(urn3.getOptionalProps().getString("stderr"), is("/logs/error.log"));

        assertThat(urn4.getRequiredProps().keySet().size(), is(0));
        assertThat(urn4.getOptionalProps().keySet().size(), is(0));

        assertThat(urn5.getRequiredProps().keySet().size(), is(0));
        assertThat(urn5.getOptionalProps().keySet().size(), is(1));
        assertThat(urn5.getOptionalProps().getString("abc"), is("def"));

        // when reverse convert to string, urn should sort property keys in lexicographical order
        assertThat(urn1.toString(), is("mysql:localhost:db=test,port=3306,table=company"));
        assertThat(urn2.toString(), is("postgres:192.168.1.2:db=test,port=5432,schema=dm,table=company"));
        assertThat(urn3.toString(), is("file:127.0.0.1:path=/data/bin/abc.bin:stderr=/logs/error.log,stream=true"));
        assertThat(urn4.toString(), is("ssh:192.168.1.5"));
        assertThat(urn5.toString(), is("ssh:192.168.1.5::abc=def"));
    }

    @Test
    public void URN_buildFromStringWithURIEncodedCharacters_shouldParseProperly() {
        // Prepare
        String urnWithEncoded1 = "https:localhost:key1=value+1,key2=value%40%21%242,key3=value%253%2c4";
        String urnWithEncoded2 = "ssh:%3a%3affff%3a0.0.0.0";

        // Process
        URN urn1 = URN.from(urnWithEncoded1);
        URN urn2 = URN.from(urnWithEncoded2);

        // Validate
        Props urn1Props = urn1.getRequiredProps();
        assertThat(urn1Props.getString("key1"), is("value 1"));
        assertThat(urn1Props.getString("key2"), is("value@!$2"));
        assertThat(urn1Props.getString("key3"), is("value%3,4"));

        assertThat(urn2.getHostname(), is("::ffff:0.0.0.0"));

        // when convert to string, special characters should be encoded
        assertThat(urn1.toString(), is(urnWithEncoded1));
        assertThat(urn2.toString(), is(urnWithEncoded2));
    }

    @Test
    public void URN_buildFromBuilder_shouldEquivalentToBuildFromString() {
        // Prepare
        Props urn1RequiredProps = new Props();
        urn1RequiredProps.put("port", 27017);
        urn1RequiredProps.put("db", "test");
        urn1RequiredProps.put("collection", "company");

        URN urn = URN.newBuilder()
                .withHostname("localhost")
                .withProtocolType("mongodb")
                .withRequiredProps(urn1RequiredProps)
                .withOptionalProps(new Props())
                .build();
        URN urnFromString = URN.from("mongodb:localhost:port=27017,db=test,collection=company");

        // Validate
        assertEquals(urn, urnFromString);
        assertEquals("mongodb:localhost:collection=company,db=test,port=27017", urnFromString.toString());
        assertEquals(urn.toString(), urnFromString.toString());
    }
}
