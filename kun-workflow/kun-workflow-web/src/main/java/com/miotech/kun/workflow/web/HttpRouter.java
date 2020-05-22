package com.miotech.kun.workflow.web;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;


class HttpRouter {
    private Logger logger = LoggerFactory.getLogger(HttpRouter.class);

    // controller method for specific request route
    private final Map<HttpRoute, HttpAction> routeMappings = new ConcurrentHashMap<>();

    private final Injector injector;

    @Inject
    public HttpRouter(Injector injector) {
        this.injector = injector;
    }

    /**
     * scan for all routesMapping under a package
     * @param packageName
     */
    public void scanPackage(String packageName) {
        final ClassLoader loader = Thread.currentThread()
                .getContextClassLoader();
        try {
            ClassPath classPath = ClassPath.from(loader);
            for (ClassPath.ClassInfo classInfo: classPath.getTopLevelClasses(packageName)) {
                this.addRouter(Class.forName(classInfo.getName()));
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to add router in package {}", packageName, e);
            ExceptionUtils.wrapIfChecked(e);
        }
    }

    /**
     * add routesMapping for a controller class
     */
    private void addRouter(Class<?> clz) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method invokeMethod : methods) {
            Annotation[] annotations = invokeMethod.getAnnotationsByType(RouteMapping.class);
            for (Annotation annotation : annotations) {
                RouteMapping requestMapping = (RouteMapping) annotation;
                String uri = requestMapping.url();
                String httpMethod = requestMapping.method().toUpperCase();

                Object instance = injector.getInstance(clz);
                HttpAction action = new HttpAction(instance, invokeMethod);
                HttpRoute route = new HttpRoute(uri, HttpMethod.resolve(httpMethod.toUpperCase()), true);
                logger.info("Found Request mapping for {} -> {}.{}",
                        route.toString(),
                        clz.getCanonicalName(),
                        invokeMethod.getName()
                );
                if (routeMappings.get(route) != null) {
                    throw new IllegalStateException("Found duplicate route mapping for: " + route.toString());
                }
                routeMappings.put(route, action);
            }
        }
    }

    public HttpAction getAction(HttpRoute route) {
        return routeMappings.get(route);
    }

    public HttpRequestMappingHandler getRequestMappingHandler(HttpServletRequest request) {
        HttpRoute route = new HttpRoute(request.getRequestURI(), HttpMethod.resolve(request.getMethod()));
        // using exactly match first, then do pattern match
        HttpAction action = getAction(route);
        if (action != null) {
            return new HttpRequestMappingHandler(route, action,
                    new HttpRequest(request, null));
        } else {
            return extractByPattern(request, route);
        }
    }

    private HttpRequestMappingHandler extractByPattern(HttpServletRequest request, HttpRoute requestRoute) {
        String requestUrl = requestRoute.getUrl();

        for (HttpRoute route: routeMappings.keySet()) {

            Matcher requestMatcher = route.getUrlPattern().matcher(requestUrl);
            if (requestMatcher.find()
                    && route.getMethod().equals(requestRoute.getMethod())) {

                Map<String, String> pathVariables = new HashMap<>();
                for (int i = 1; i <= requestMatcher.groupCount(); i++) {
                    String pathVariableName = route.getPathVariablePlaceHolder().get(i-1);
                    pathVariables.put(pathVariableName, requestMatcher.group(i));
                }

                return new HttpRequestMappingHandler(route, getAction(route),
                        new HttpRequest(request, pathVariables));
            }
        }
        return null;
    }
}
