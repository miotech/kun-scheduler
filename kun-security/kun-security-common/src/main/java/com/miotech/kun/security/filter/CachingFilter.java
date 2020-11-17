package com.miotech.kun.security.filter;

import com.miotech.kun.security.common.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author: Jie Chen
 * @created: 2020/11/16
 */
public class CachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        chain.doFilter(requestWrapper, response);
    }
}
