package com.miotech.kun.commons.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.containsInRelativeOrder;
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
        props.put("stringList","one,two,three");
        assertThat(props.getInt("int"), is(333));
        assertThat(props.getString("str"), is("sss"));
        assertThat(props.getBoolean("boolean"), is(true));
        assertThat(props.getLong("long"), is(435464567352l));
        assertThat(props.getDouble("double"), is(345654.456756));
        assertThat(props.getStringList("stringList"),containsInRelativeOrder("one","two","three"));
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

    @Test
    public void testAddProps_shouldCoverOriginalValues(){
        //prepare original values
        Props props1 = new Props();
        props1.put("str", "sss");
        props1.put("int", "333");
        props1.put("boolean", true);
        props1.put("long", 435464567352l);
        props1.put("double", 345654.456756);

        //prepare new values
        Props props2 = new Props();
        props1.put("str", "ttt");
        props1.put("int", "222");
        props1.put("boolean", false);
        //cover originalValues
        props1.addProps(props2);

        //verify
        assertThat(props1.getString("str"), is("ttt"));
        assertThat(props1.getInt("int"), is(222));
        assertThat(props1.getBoolean("boolean"), is(false));
        assertThat(props1.getLong("long"), is(435464567352l));
        assertThat(props1.getDouble("double"), is(345654.456756));
    }

    @Test
    public void testPropsToProperties(){
        //prepare original values
        Props props1 = new Props();
        props1.put("str", "sss");
        props1.put("int", "333");
        props1.put("boolean", true);
        props1.put("long", 435464567352l);
        props1.put("double", 345654.456756);

        //prepare new values
        Props props2 = new Props();
        props1.put("str", "ttt");
        props1.put("int", "222");
        props1.put("boolean", false);
        //cover originalValues
        props1.addProps(props2);

        //convert props to properties
        Properties properties = props1.toProperties();

        //verify
        assertThat(properties.getProperty("str"), is("ttt"));
        assertThat(Integer.valueOf(properties.getProperty("int")), is(222));
        assertThat(Boolean.valueOf(properties.getProperty("boolean")), is(false));
        assertThat(Long.valueOf(properties.getProperty("long")), is(435464567352l));
        assertThat(Double.valueOf(properties.getProperty("double")), is(345654.456756));
    }

    @Test
    public void testCreatePropsWithMultiMaps_laterMapShouldCoverFront(){
        //prepare values
        Map<String,String> map1 = new HashMap<>();
        map1.put("str", "sss");
        map1.put("int", "333");
        map1.put("boolean", "true");
        map1.put("long", "435464567352");
        map1.put("double", "345654.456756");
        Map<String,String> map2 = new HashMap<>();
        map2.put("str", "ttt");
        map2.put("int", "222");
        map2.put("boolean", "false");
        map2.put("long", "435464567352");
        map2.put("double", "345654.456756");

        Props props = new Props(map1,map2);
        //verify
        assertThat(props.getString("str"), is("ttt"));
        assertThat(props.getInt("int"), is(222));
        assertThat(props.getBoolean("boolean"), is(false));
        assertThat(props.getLong("long"), is(435464567352l));
        assertThat(props.getDouble("double"), is(345654.456756));
    }

}
