package com.miotech.kun.workflow.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.http.*;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DispatchServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(DispatchServlet.class);

    private HttpRouter router;

    private ObjectMapper objectMapper;

    private final Injector injector;

    @Inject
    public DispatchServlet(Injector injector, HttpRouter router, ObjectMapper objectMapper) {
        this.injector = injector;
        this.router = router;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init() {
        String basePackageName = this.getClass().getPackage().getName();
        router.scanPackage(basePackageName + ".controller");
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());

        if (HttpMethod.HEAD == httpMethod) {
            // TODO: handle HEAD request
        }
        doAction(request, response, httpMethod);
    }


    private void doAction(final HttpServletRequest req,
                          final HttpServletResponse resp,
                          HttpMethod method)
            throws ServletException, IOException {
        logger.debug("Receive request {} {}", method.toString(), req.getRequestURL());
        try {
            HttpRequestMappingHandler handler = router.getRequestMappingHandler(req);
            if (handler != null) {
                // Invoke method with args
                List<Object> args = new ArrayList<>();
                HttpAction action = handler.getAction();
                for (Parameter parameter: action.getMethod().getParameters()) {
                    args.add(resolveParameter(parameter, handler.getHttpRequest(), resp));
                }

                Object responseObj = action.call(args.toArray());
                doResponse(responseObj, req, resp);

            } else {
                String errorMsg = "Cannot resolve url mapping for: " + req.getRequestURI();
                logger.info(errorMsg);
                InternalErrorMessage errorMessage = new InternalErrorMessage(
                        errorMsg,
                        req.getRequestURI(),
                        LocalDateTime.now(),
                        "Resource Not Found",
                        400
                );
                doError(errorMessage, req, resp);
            }
        } catch (Exception e) {
            logger.error("Server error in process: {} {} ",
                    req.getMethod(),
                    req.getRequestURI(), e);
            InternalErrorMessage errorMessage = new InternalErrorMessage(
                    e.getMessage(),
                    req.getRequestURI(),
                    LocalDateTime.now()
            );
            doError(errorMessage, req, resp);
        }
    }

    private void doError(InternalErrorMessage errorMessage,
                         final HttpServletRequest req,
                         final HttpServletResponse resp) throws IOException {
        resp.setStatus(errorMessage.getStatus());
        doResponse(errorMessage, req, resp);
    }

    private void doResponse(Object responseObj,
                            final HttpServletRequest req,
                            final HttpServletResponse resp) throws IOException {
        if (resp.getStatus() <= 0) {
            resp.setStatus(HttpStatus.OK_200);
        }

        if (responseObj != null) {
            PrintWriter out = resp.getWriter();
            // TODO: read content type from header and respond different format
            resp.setContentType("application/json;charset=utf-8");
            resp.setCharacterEncoding("UTF-8");

            out.print(objectMapper.writeValueAsString(responseObj));
            out.flush();
        }
    }

    private Object resolveParameter( Parameter parameter,
                                     final HttpRequest req,
                                     final HttpServletResponse resp) throws IOException {
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
                return objectMapper.readValue(httpServletRequest.getInputStream(), paramClz);
            }
            if (annotation.annotationType() == RouteVariable.class) {
                String pathVariable = req.getPathVariables().get(parameter.getName());
                switch (paramClz.getName()) {
                    case "java.lang.String":
                        return pathVariable;
                    case "int":
                    case "java.lang.Integer":
                        return Integer.parseInt(pathVariable);
                    case "long":
                    case "java.lang.Long":
                        return Long.parseLong(pathVariable);
                    default:
                        throw new RuntimeException("RouteVariable can not be type: " + paramClz.getName());
                }
            }
        }
        return null;
    }
}
