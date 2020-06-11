package com.miotech.kun.workflow.web.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRoute {
    private final String PATH_SPLITOR = "/";
    private final Pattern pathPattern = Pattern.compile("\\{(.*)}");

    final private String url;

    final private HttpMethod method;

    private final List<String> pathVariablePlaceHolder;

    private final Pattern urlPattern;

    public HttpRoute(String url, HttpMethod method) {
        this(url, method, false);
    }

    public HttpRoute(String url, HttpMethod method, Boolean parseUrl)  {
        this.url = url;
        this.method = method;
        if  (parseUrl) {
            String[] patterns = getUrl().split(PATH_SPLITOR);
            List<String> testMatchPattern = new ArrayList<>();
            pathVariablePlaceHolder = new ArrayList<>();

            for (String pattern: patterns) {
                Matcher matcher = pathPattern.matcher(pattern);
                if (matcher.find()) {
                    pathVariablePlaceHolder.add(matcher.group(1));
                    testMatchPattern.add("([^\\/]+)");
                } else {
                    testMatchPattern.add(pattern);
                }
            }

            String testPath = String.join(PATH_SPLITOR, testMatchPattern);
            urlPattern = Pattern.compile("^" + testPath + "$");
        } else {
            urlPattern = null;
            pathVariablePlaceHolder = null;
        }
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Pattern getUrlPattern() {
        return urlPattern;
    }

    public List<String> getPathVariablePlaceHolder() {
        return pathVariablePlaceHolder;
    }


    @Override
    public String toString() {
        return getMethod().toString() + " " + getUrl();
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