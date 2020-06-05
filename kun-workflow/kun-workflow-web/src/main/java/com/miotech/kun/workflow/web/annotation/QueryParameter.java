package com.miotech.kun.workflow.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParameter {
    /**
     * name of query parameter
     */
    String name() default "";

    /**
     * whether query parameter is required in request.
     * throw exception when not displayed if required is true
     * else return null
     */
    boolean required() default false;

    /**
     * default value when parameter required is false
     * but not provided in request
     */
    String defaultValue() default VALUE_DEFAULT.DEFAULT_NONE;
}
