package com.miotech.kun.workflow.web.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class HttpAction {
    private Logger logger = LoggerFactory.getLogger(HttpAction.class);

    // controller 单例对象
    final private Object object;

    // 调用方法
    final private Method method;

    final private boolean needInjestRequest;

    final private boolean needInjestResp;

    public boolean getNeedInjectionRequest() { return needInjestRequest; };


    public boolean getNeedInjectionResp() { return needInjestResp; };

    public HttpAction (Object object,Method method) {
        this.object = object;
        this.method = method;
        Class[] params = method.getParameterTypes();
        needInjestRequest = Arrays.stream(params)
                .anyMatch(x -> x == HttpServletRequest.class);
        needInjestResp = Arrays.stream(params)
                .anyMatch(x -> x == HttpServletResponse.class);
    }

    public Object call(Object... args) {
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("{}", e);
        }
        return null;
    }
}
