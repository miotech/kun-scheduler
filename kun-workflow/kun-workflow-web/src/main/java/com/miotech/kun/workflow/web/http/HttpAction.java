package com.miotech.kun.workflow.web.http;

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

    public Object call(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(object, args);
    }
}
