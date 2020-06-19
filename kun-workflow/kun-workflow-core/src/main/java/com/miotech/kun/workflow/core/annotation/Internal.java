package com.miotech.kun.workflow.core.annotation;

import java.lang.annotation.*;

/**
 * Signifies that a publicly accessible API (public class, method or field) is intended for internal
 * use only and not for public consumption.
 *
 * <p>Such an API is subject to incompatible changes or removal at any time.
 */
@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PACKAGE,
        ElementType.TYPE
})
@Documented
public @interface Internal {
}
