package com.miotech.kun.workflow.web;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.BadRequestException;
import com.miotech.kun.workflow.web.annotation.QueryParameter;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.annotation.VALUE_DEFAULT;
import com.miotech.kun.workflow.web.http.HttpRequest;
import com.miotech.kun.workflow.web.serializer.JsonSerializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Singleton
class ParameterResolver {
    private final Logger logger = LoggerFactory.getLogger(ParameterResolver.class);

    @Inject
    private JsonSerializer jsonSerializer;

    /**
     * Resolve parameter and value declared in handler method from HttpAction
     * {@link com.miotech.kun.workflow.web.http.HttpAction}
     * Currently support parameter type:
     *  HttpServletRequest, HttpServletResponse
     * or Annotation:
     *   @RequestBody, QueryParameter, RouteVariable
     *
     * @return Object
     * @throws IOException
     */
    Object resolveRequestParameter(Parameter parameter,
                                   final HttpRequest req,
                                   final HttpServletResponse resp) throws IOException {
        logger.debug("Resolve Request parameter {} {}", parameter.getType(), parameter.getName());
        Class<?> paramClz = parameter.getType();
        HttpServletRequest httpServletRequest = req.getHttpServletRequest();
        if (paramClz == HttpServletRequest.class) {
            return req.getHttpServletRequest();
        }

        if (paramClz == HttpServletResponse.class) {
            return resp;
        }

        for (Annotation annotation: parameter.getAnnotations()) {
            if (annotation.annotationType() == RequestBody.class) {
                logger.debug("Resolve parameter from request body parameter");
                try {
                    return jsonSerializer.toObject(httpServletRequest.getInputStream(), paramClz);
                } catch (RuntimeException e) {
                    throw new BadRequestException(e);
                }
            }
            if (annotation.annotationType() == QueryParameter.class) {
                logger.debug("Resolve parameter from request query parameter");
                return resolveQueryParameter(req.getHttpServletRequest(), parameter, annotation);
            }
            if (annotation.annotationType() == RouteVariable.class) {
                logger.debug("Resolve parameter from route url");
                String pathVariable = req.getPathVariables().get(parameter.getName());
                return toParameterValue(parameter, pathVariable);
            }
        }
        return null;
    }

    private Object resolveQueryParameter(HttpServletRequest req,
                                         Parameter parameter,
                                         Annotation annotation) {
        QueryParameter queryParameter = ((QueryParameter) annotation);
        String defaultValue = queryParameter.defaultValue();
        String parameterName = queryParameter.name();
        if (StringUtils.isBlank(parameterName)) {
            parameterName = parameter.getName();
        }

        String parameterValue = req.getParameter(parameterName);
        if (parameterValue == null) {
            if (queryParameter.required()) {
                throw new BadRequestException("No parameter specified while defined as required " + parameterName);
            }
            if (!defaultValue.equals(VALUE_DEFAULT.DEFAULT_NONE)) {
                parameterValue = defaultValue;
            }
        }
        return toParameterValue(parameter, parameterValue);
    }

    private Object toParameterValue(Parameter parameter, String variable) {
        return toParameterValue(parameter, parameter.getType().getName(), variable);
    }

    private Object toParameterValue(Parameter parameter, String paramClzName, String variable) {
        if (variable == null) return null;

        switch (paramClzName) {
            case "java.lang.String":
                return variable;
            case "int":
            case "java.lang.Integer":
                return Integer.parseInt(variable);
            case "long":
            case "java.lang.Long":
                return Long.parseLong(variable);
            case "java.util.List":
                return toParameterListValues(parameter, variable);
            default:
                throw new BadRequestException("Parameter can not be type: " + paramClzName);
        }
    }

    private List<Object> toParameterListValues(Parameter parameter, String variable) {
        ParameterizedType paramType = (ParameterizedType) parameter.getParameterizedType();
        Type[] paramArgTypes = paramType.getActualTypeArguments();
        if (paramArgTypes.length == 0) {
            throw new IllegalStateException("Cannot get actual type for generic parameter type: List");
        }
        String[] argumentList = variable.split(",");
        return Lists.newArrayList(argumentList)
                .stream()
                .map(x -> toParameterValue(null, paramArgTypes[0].getTypeName(), x))
                .collect(Collectors.toList());
    }
}
