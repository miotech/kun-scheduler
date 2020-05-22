package com.miotech.kun.workflow.web.http;

public class HttpRequestMappingHandler {
    private final HttpRoute route;

    private final HttpAction action;

    private final HttpRequest httpRequest;

    public HttpRequestMappingHandler(HttpRoute route,
                                     HttpAction action,
                                     HttpRequest httpRequest) {
        this.route = route;
        this.action = action;
        this.httpRequest = httpRequest;
    }


    public HttpAction getAction() {
        return action;
    }

    public HttpRoute getRoute() {
        return route;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }
}
