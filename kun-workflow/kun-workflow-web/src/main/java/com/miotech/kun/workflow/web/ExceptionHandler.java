package com.miotech.kun.workflow.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.BadRequestException;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.exception.ExceptionResponse;
import com.miotech.kun.workflow.common.exception.NameConflictException;
import com.miotech.kun.workflow.web.annotation.ResponseException;
import com.miotech.kun.workflow.web.annotation.ResponseStatus;
import com.miotech.kun.workflow.web.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class ExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    private final Map<Class<?>, Method> exceptionHandlers = scanHandler();

    @Inject
    private JsonSerializer jsonSerializer;

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
                method.invoke(this, args.toArray());
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

    @ResponseStatus(code = 400)
    @ResponseException(IllegalArgumentException.class)
    private void illegalArgHandler(HttpServletResponse resp,
                                   IllegalArgumentException e) {
        ExceptionResponse responseObj = new ExceptionResponse(400, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }

    @ResponseStatus(code = 404)
    @ResponseException(EntityNotFoundException.class)
    private void entityNotFoundHandler(HttpServletResponse resp,
                                   EntityNotFoundException e) {
        ExceptionResponse responseObj = new ExceptionResponse(404, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }

    @ResponseStatus(code = 409)
    @ResponseException(NameConflictException.class)
    private void namingConflictException(HttpServletResponse resp,
                                         NameConflictException e) {
        ExceptionResponse responseObj = new ExceptionResponse(409, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }

    @ResponseStatus(code = 400)
    @ResponseException(BadRequestException.class)
    private void badRequestHandler(HttpServletResponse resp,
                                   BadRequestException e) {
        ExceptionResponse responseObj = new ExceptionResponse(400, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }

    @ResponseStatus(code = 400)
    @ResponseException(DateTimeParseException.class)
    private void dateTimeParseExceptionHandler(HttpServletResponse resp,
                                               DateTimeParseException e) {
        ExceptionResponse responseObj = new ExceptionResponse(400, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }

    private Map<Class<?>, Method> scanHandler() {
        Map<Class<?>, Method> exceptionHandlers = new HashMap<>();
        Method[] methods = ExceptionHandler.class.getDeclaredMethods();
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
