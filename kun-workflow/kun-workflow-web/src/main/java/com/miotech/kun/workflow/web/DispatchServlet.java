package com.miotech.kun.workflow.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.http.InternalErrorMessage;
import com.miotech.kun.workflow.web.http.HttpAction;
import com.miotech.kun.workflow.web.http.HttpMethod;
import com.miotech.kun.workflow.web.http.HttpRoute;
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
        HttpRoute route = new HttpRoute(req.getRequestURI(), method);
        HttpAction action = router.getRoute(route);

        if (action != null) {

            // Invoke method with args
            try {
                List<Object> args = new ArrayList<>();
                for (Parameter parameter: action.getMethod().getParameters())
                    args.add(resolveParameter(parameter, req, resp));

                Object responseObj = action.call(args.toArray());
                doResponse(responseObj, req, resp);
            } catch (Exception e) {
                logger.error("Server error in process: {} {} ",
                        route.getMethod(),
                        req.getRequestURI(), e);
                InternalErrorMessage errorMessage = new InternalErrorMessage(
                        e.getMessage(),
                        req.getRequestURI(),
                        LocalDateTime.now()
                );
                resp.setStatus(errorMessage.getStatus());
                doResponse(errorMessage, req, resp);
            }
        } else {
            // TODO: respond with resource not found
            logger.debug("Cannot resolve url mapping for {}", req.getRequestURI());
        }
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
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            out.print(objectMapper.writeValueAsString(responseObj));
            out.flush();
        }
    }

    private Object resolveParameter( Parameter parameter,
                                     final HttpServletRequest req,
                                     final HttpServletResponse resp) throws IOException {
        Class<?> paramClz = parameter.getType();
        if (paramClz == HttpServletRequest.class) {
            return req;
        }

        if (paramClz == HttpServletResponse.class) {
            return resp;
        }

        for (Annotation annotation: parameter.getAnnotations()) {
            if (annotation.annotationType() == RequestBody.class) {
                return objectMapper.readValue(req.getInputStream(), paramClz);
            }
        }
        return null;
    }
}
