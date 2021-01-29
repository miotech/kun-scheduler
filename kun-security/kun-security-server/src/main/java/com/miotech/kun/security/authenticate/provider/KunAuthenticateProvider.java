package com.miotech.kun.security.authenticate.provider;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author: Jie Chen
 * @created: 2020/8/24
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface KunAuthenticateProvider {

    String value() default "kunAuthProvider";

}