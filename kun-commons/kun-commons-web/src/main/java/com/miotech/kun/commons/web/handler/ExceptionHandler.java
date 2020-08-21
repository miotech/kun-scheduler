package com.miotech.kun.commons.web.handler;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.web.annotation.ResponseException;
import com.miotech.kun.commons.web.annotation.ResponseStatus;
import com.miotech.kun.commons.web.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class ExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    private final Map<Class<?>, Method> exceptionHandlers = new ConcurrentHashMap<>();

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    private Injector injector;

    public Throwable handleException(HttpServletRequest request,
                                HttpServletResponse response,
                                Throwable e) {
        Class<?> exceptionClz = e.getClass();
        Method method = exceptionHandlers.get(exceptionClz);
        if (method != null) {
            Parameter[] parameters = method.getParameters();
            logger.debug("Handle Exception {} using method: {}({})",
                    exceptionClz.getName(),
                    method.getName(),
                    Arrays.stream(parameters).map(x -> x.getType() + x.getName())
                            .collect(Collectors.joining(",")));
            List<Object> args = new ArrayList<>();
            for (Parameter parameter: parameters) {
                if (Throwable.class.isAssignableFrom(parameter.getType())) {
                    args.add(e);
                }
                if (parameter.getType() == HttpServletRequest.class) {
                    args.add(request);
                }
                if (parameter.getType() == HttpServletResponse.class) {
                    args.add(response);
                }
            }

            try {
                Annotation responseStatus = method.getAnnotation(ResponseStatus.class);
                if (responseStatus != null) {
                    response.setStatus(((ResponseStatus) responseStatus).code());
                }
                Object instance = injector.getInstance(method.getDeclaringClass());
                method.invoke(instance, args.toArray());
            } catch (IllegalAccessException ex) {
                return ex;
            } catch (InvocationTargetException ex) {
                return ex.getCause();
            }
            return null;
        } else {
            return e;
        }
    }

    public void scanPackage(String packageName) {
        final ClassLoader loader = Thread.currentThread()
                .getContextClassLoader();
        try {
            ClassPath classPath = ClassPath.from(loader);
            for (ClassPath.ClassInfo classInfo: classPath.getTopLevelClassesRecursive(packageName)) {
                this.scanHandler(Class.forName(classInfo.getName()));
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to add exception handler in package {}", packageName, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private Map<Class<?>, Method> scanHandler(Class<?> clz) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method method: methods) {
           Annotation exceptionAnnotation = method.getAnnotation(ResponseException.class);
           if (exceptionAnnotation != null) {
               Class<?> exceptionClz = ((ResponseException) exceptionAnnotation).value();
               logger.info("Found exception handler method {} for exception: {}", method.getName(), exceptionClz.getName());

               for (Parameter param: method.getParameters()) {
                   if (param.getType() != HttpServletRequest.class
                           || param.getType() != HttpServletResponse.class
                           || Throwable.class.isAssignableFrom(param.getType())) {
                       logger.error("Cannot resolve parameter type for {} in method {}", param.getType(), method.getName());
                   }
               }
               exceptionHandlers.put(exceptionClz, method);
           }
        }
        return exceptionHandlers;
    }
}
