package com.miotech.kun.security.testing;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockTestUserSecurityContextFactory.class)
public @interface WithMockTestUser {

    long id() default 1L;

    String name() default "tester";

    String password() default "password";

    String[] permissions() default "";
}
