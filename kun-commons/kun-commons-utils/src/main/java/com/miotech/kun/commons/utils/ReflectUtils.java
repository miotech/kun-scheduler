package com.miotech.kun.commons.utils;

import java.lang.reflect.Field;

public class ReflectUtils {

    public static <T> void setField(T object,String fieldName,T value) throws IllegalAccessException,NoSuchFieldException{
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object,value);
    }

}
