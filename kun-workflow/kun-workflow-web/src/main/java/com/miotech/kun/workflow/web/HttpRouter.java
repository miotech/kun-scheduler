package com.miotech.kun.workflow.web;

import com.google.common.reflect.ClassPath;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.http.HttpAction;
import com.miotech.kun.workflow.web.http.HttpMethod;
import com.miotech.kun.workflow.web.http.HttpRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.miotech.kun.workflow.web.ServerContext.SERVER_CONTEXT;

class HttpRouter {
    private Logger logger = LoggerFactory.getLogger(HttpRouter.class);

    // controller method for specific request route
    private Map<HttpRoute, HttpAction> routeMappings = new HashMap<>();

    /**
     * scan for all routesMapping under a package
     * @param packageName
     */
    public void scanPackage(String packageName) throws IOException {
        final ClassLoader loader = Thread.currentThread()
                .getContextClassLoader();
        ClassPath classPath = ClassPath.from(loader);
        classPath.getTopLevelClasses(packageName)
        .stream()
        .forEach(x -> {
            try {
                this.addRouter(Class.forName(x.getName()));
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                logger.error("Failed to add router in class {}", x.getName(), e);
            }
        });
    }

    /**
     * add routesMapping for a controller class
     */
    private void addRouter(Class<?> clz) throws IllegalAccessException, InstantiationException {
        Method[] methods = clz.getDeclaredMethods();
        for (Method invokeMethod : methods) {
            Annotation[] annotations = invokeMethod.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == RouteMapping.class) {
                    RouteMapping requestMapping = (RouteMapping) annotation;
                    String uri = requestMapping.url();
                    String httpMethod = requestMapping.method().toUpperCase();

                    Object instance = SERVER_CONTEXT.getInstance(clz);
                    HttpAction action = new HttpAction(instance, invokeMethod);
                    HttpRoute route = new HttpRoute(uri, HttpMethod.resolve(httpMethod.toUpperCase()));
                    logger.info("Found Request mapping for {} -> {}.{}",
                            route.toString(),
                            clz.getCanonicalName(),
                            invokeMethod.getName()
                    );
                    if (routeMappings.get(route) != null) {
                        throw new RuntimeException("Found duplicate route mapping for: " + route.toString());
                    }
                    routeMappings.put(route, action);
                }
            }
        }
    }

    public HttpAction getRoute(HttpRoute route) {
        return routeMappings.get(route);
    }
}
