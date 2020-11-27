package com.miotech.kun.security.filter;

import com.miotech.kun.security.service.BaseSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author: Jie Chen
 * @created: 2020/11/27
 */
@Slf4j
public class LogFilter implements Filter {

    private BaseSecurityService securityService;

    public LogFilter() {
        this.securityService = new BaseSecurityService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {

            HttpServletRequest httpServletRequest = (HttpServletRequest) request;

            String path = httpServletRequest.getRequestURI();
            path = new StringJoiner("", "[", "]").add(path).toString();

            String method = httpServletRequest.getMethod();
            method = new StringJoiner("", "[", "]").add(method).toString();

            String parameters = mapToString(httpServletRequest.getParameterMap());
            parameters = new StringJoiner("", "[", "]").add(parameters).toString();

            CachingRequestWrapper requestWrapper = new CachingRequestWrapper(httpServletRequest);
            request = requestWrapper;
            String requestBody = requestWrapper.getBody();
            requestBody = new StringJoiner("", "[", "]").add(requestBody).toString();

            String username = securityService.getCurrentUsername();
            username = new StringJoiner("", "[", "]").add(username).toString();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Request Logging - Username=");
            stringBuilder.append(username);
            stringBuilder.append(" Path=");
            stringBuilder.append(path);
            stringBuilder.append(" Method=");
            stringBuilder.append(method);
            stringBuilder.append(" Request Parameters=");
            stringBuilder.append(parameters);
            stringBuilder.append(" Request Body=");
            stringBuilder.append(requestBody);
            log.info(stringBuilder.toString());
        }
        chain.doFilter(request, response);
    }

    private String mapToString(Map<String, String[]> map) {
        if (map == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String[]>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String[]> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=');
            StringJoiner stringJoiner = new StringJoiner(",");
            for (String val : entry.getValue()) {
                stringJoiner.add(val);
            }
            sb.append(stringJoiner.toString());
            if (iter.hasNext()) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private class CachingRequestWrapper extends HttpServletRequestWrapper {

        private final String body;

        public CachingRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.body = getBodyString(request);
        }

        public String getBody() {
            return body;
        }

        public String getBodyString(final HttpServletRequest request) throws IOException {
            String contentType = request.getContentType();
            if (StringUtils.isNotBlank(contentType)
                    && !contentType.contains("multipart/form-data")
                    && !contentType.contains("x-www-form-urlencoded")) {
                return IOUtils.toString(request.getInputStream(), getCharacterEncoding());
            } else {
                return "";
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes());

            return new ServletInputStream() {

                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public int read() {
                    return bais.read();
                }

                @Override
                public void setReadListener(ReadListener readListener) {

                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(this.getInputStream()));
        }

    }
}
