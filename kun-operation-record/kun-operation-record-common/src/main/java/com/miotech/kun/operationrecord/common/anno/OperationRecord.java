package com.miotech.kun.operationrecord.common.anno;

import com.miotech.kun.operationrecord.common.model.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperationRecord {

    String[] args() default "";

    OperationType type();

}
