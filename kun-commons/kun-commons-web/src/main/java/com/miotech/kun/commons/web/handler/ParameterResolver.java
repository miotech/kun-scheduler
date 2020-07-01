package com.miotech.kun.commons.web.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.commons.web.annotation.VALUE_DEFAULT;
import com.miotech.kun.commons.web.http.HttpRequest;
import com.miotech.kun.commons.web.serializer.JsonSerializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;


@Singleton
class ParameterResolver {
    private final Logger logger = LoggerFactory.getLogger(ParameterResolver.class);

    @Inject
    private JsonSerializer jsonSerializer;

    /**
     * Resolve parameter and value declared in handler method from HttpAction
     * {@link com.miotech.kun.commons.web.http.HttpAction}
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
                return jsonSerializer.toObject(httpServletRequest.getInputStream(), paramClz);
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
                throw new RuntimeException("No parameter specified while defined as required " + parameterName);
            }
            if (!defaultValue.equals(VALUE_DEFAULT.DEFAULT_NONE)) {
                parameterValue = defaultValue;
            }
        }
        return toParameterValue(parameter, parameterValue);
    }

    private Object toParameterValue(Parameter parameter, String variable) {
        if (variable == null) return null;
        String paramClzName = parameter.getType().getName();

        switch (paramClzName) {
            case "java.lang.String":
                return variable;
            case "int":
            case "java.lang.Integer":
                return Integer.parseInt(variable);
            case "long":
            case "java.lang.Long":
                return Long.parseLong(variable);
            default:
                throw new RuntimeException("Parameter can not be type: " + paramClzName);
        }
    }
}
