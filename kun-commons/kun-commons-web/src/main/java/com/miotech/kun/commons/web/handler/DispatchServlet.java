package com.miotech.kun.commons.web.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.BasePackageScan;
import com.miotech.kun.commons.web.http.*;
import com.miotech.kun.commons.web.serializer.JsonSerializer;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Singleton
public class DispatchServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(DispatchServlet.class);

    @Inject
    private HttpRouter router;

    @Inject
    private ParameterResolver parameterResolver;

    @Inject
    private ExceptionHandler exceptionHandler;

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    @BasePackageScan
    private String basePackageName;

    @Override
    public void init() {
        router.scanPackage(basePackageName);
        exceptionHandler.scanPackage(basePackageName);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());

        if (HttpMethod.HEAD == httpMethod) {
            // TODO: handle HEAD request
        }
        doAction(request, response, httpMethod);
    }


    private void doAction(final HttpServletRequest req,
                          final HttpServletResponse resp,
                          HttpMethod method) {
        logger.debug("Receive request {} {}", method.toString(), req.getRequestURL());
        try {
            HttpRequestMappingHandler handler = router.getRequestMappingHandler(req);
            if (handler != null) {
                // Invoke method with args
                List<Object> args = new ArrayList<>();
                HttpAction action = handler.getAction();
                for (Parameter parameter: action.getMethod().getParameters()) {
                    args.add(parameterResolver.resolveRequestParameter(
                            parameter,
                            handler.getHttpRequest(),
                            resp));
                }

                Object responseObj = action.call(args.toArray());
                doResponse(responseObj, resp);
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
                doError(errorMessage, resp);
            }
        } catch (Throwable e) {
            logger.debug("{}, Handle exception {}", req.getRequestURI(), e.getClass().getName());
            handleException(req, resp, e);
        }

    }

    private void handleException(HttpServletRequest req,
                                 HttpServletResponse resp,
                                 Throwable e) {
        Throwable finalExeception;

        try {
            finalExeception = exceptionHandler.handleException(req, resp, e);
        } catch (Exception ex) {
            finalExeception = ex;
        }
        if (finalExeception != null) {
            logger.error("Server error in process: {} {} ",
                    req.getMethod(),
                    req.getRequestURI(), e);
            InternalErrorMessage errorMessage = new InternalErrorMessage(
                    e.getMessage(),
                    req.getRequestURI(),
                    LocalDateTime.now()
            );
            doError(errorMessage, resp);
        }
    }

    private void doError(InternalErrorMessage errorMessage,
                         final HttpServletResponse resp) {
        resp.setStatus(errorMessage.getStatus());
        doResponse(errorMessage, resp);
    }

    private void doResponse(Object responseObj,
                            final HttpServletResponse resp) {
        if (resp.getStatus() <= 0) {
            resp.setStatus(HttpStatus.OK_200);
        }

        if (responseObj != null) {
            // TODO: read content type from header and respond different format
            jsonSerializer.writeResponseAsJson(resp, responseObj);
        }
    }
}
