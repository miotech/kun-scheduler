package com.miotech.kun.security.common;

import com.miotech.kun.security.service.BaseSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author: Jie Chen
 * @created: 2020/8/7
 */
@Slf4j
public class LogInterceptor extends HandlerInterceptorAdapter {

    private BaseSecurityService securityService;

    public LogInterceptor() {
        this.securityService = new BaseSecurityService();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String path = request.getRequestURI();
        path = new StringJoiner("", "[", "]").add(path).toString();

        String method = request.getMethod();
        method = new StringJoiner("", "[", "]").add(method).toString();

        String parameters = mapToString(request.getParameterMap());
        parameters = new StringJoiner("", "[", "]").add(parameters).toString();

        /*String requestBody = "";
        if (request.getReader() != null && request.getReader().ready()) {
            requestBody = IOUtils.toString(request.getReader());
        }
        requestBody = new StringJoiner("", "[", "]").add(requestBody).toString();*/

        String username = securityService.getCurrentUsername();
        username = new StringJoiner("", "[", "]").add(username).toString();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Request Logging - Username=");
        stringBuilder.append(username);
        stringBuilder.append(" Path=");
        stringBuilder.append(path);
        stringBuilder.append(" Method=");
        stringBuilder.append(method);
        stringBuilder.append(" Parameters=");
        stringBuilder.append(parameters);
        /*stringBuilder.append(" Body=");
        stringBuilder.append(requestBody);*/
        log.info(stringBuilder.toString());
        return super.preHandle(request, response, handler);
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
}
