package com.miotech.kun.workflow.web.http;

import java.util.Objects;

public class HttpRoute {

    final private String url;

    final private HttpMethod method;

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpRoute(String url, HttpMethod method) {
        this.url = url;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRoute httpRoute = (HttpRoute) o;
        return Objects.equals(url, httpRoute.url) &&
                method == httpRoute.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }
}