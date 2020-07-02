package com.miotech.kun.commons.web.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HttpAction {
    private Logger logger = LoggerFactory.getLogger(HttpAction.class);

    // controller 单例对象
    private final Object object;

    // 调用方法
    private final Method method;

    public Method getMethod() { return method; }

    public HttpAction (Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    public Object call(Object... args) throws Throwable {
        logger.debug("controller object = {}", object);
        logger.debug("method = {}", method.toString());
        logger.debug("args.length = {}", args.length);
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
