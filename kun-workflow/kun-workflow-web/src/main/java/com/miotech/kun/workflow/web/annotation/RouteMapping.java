package com.miotech.kun.workflow.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RouteMapping {
    /**
     * Request Url
     *
     * @return
     */
    String url();

    /**
     * Request Method
     *
     * @return
     */
    String method();
}