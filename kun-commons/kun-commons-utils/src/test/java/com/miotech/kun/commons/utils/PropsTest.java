package com.miotech.kun.commons.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class PropsTest {

    private static final Logger logger = LoggerFactory.getLogger(Props.class);

    @Test
    public void testPutAndGetValue() {
        Props props = new Props();
        props.put("str", "sss");
        props.put("int", "333");
        props.put("boolean", true);
        props.put("long", 435464567352l);
        props.put("double", 345654.456756);
        assertThat(props.getInt("int"), is(333));
        assertThat(props.getString("str"), is("sss"));
        assertThat(props.getBoolean("boolean"), is(true));
        assertThat(props.getLong("long"), is(435464567352l));
        assertThat(props.getDouble("double"), is(345654.456756));
        assertThat(props.getInt("inValidInt",123), is(123));
        assertThat(props.getString("inValidaStr","string"), is("string"));
        assertThat(props.getBoolean("inValidBoolean",false), is(false));
        assertThat(props.getLong("inValidLong",12345678910l), is(12345678910l));
        assertThat(props.getDouble("inValidDouble",1234567.891), is(1234567.891));
        try {
            props.getInt("str");
            fail("No exception thrown");
        } catch (Exception e) {
            assertTrue(e instanceof NumberFormatException);
        }
        try {
            props.getInt("num");
            fail("No exception thrown");
        } catch (Exception e){
            assertTrue(e instanceof UndefinedPropertyException);
        }
    }

    @Test
    public void testConvertPropertiesToProps(){
        Properties properties = System.getProperties();
        Props props = Props.fromProperties(properties);
        assertEquals(properties,props.toProperties());
    }

}
