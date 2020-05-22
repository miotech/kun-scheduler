package com.miotech.kun.workflow.web.http;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class HttpRequest {

    private final HttpServletRequest httpServletRequest;

    private final Map<String, String> pathVariables;

    public HttpRequest(HttpServletRequest httpServletRequest,
                       Map<String, String> pathVariables) {
        this.httpServletRequest = httpServletRequest;
        this.pathVariables = pathVariables;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }
}
