package com.miotech.kun.workflow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class ClassUtils {
    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    public static Method getGetterMethod(Class<?> clz, String attribute) {
        Optional<Method> methodOptional = Arrays.stream(clz.getMethods())
                . filter(x -> x.getName()
                        .equals("get"+ StringUtils.capitalize(attribute)))
                .findFirst();

        if (methodOptional.isPresent()) {
            return methodOptional.get();
        } else {
            String errorMsg = "Did not found getter method for attribute: " + attribute;
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }
}
