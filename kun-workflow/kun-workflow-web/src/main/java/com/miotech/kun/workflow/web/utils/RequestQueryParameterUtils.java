package com.miotech.kun.workflow.web.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class RequestQueryParameterUtils {

    private RequestQueryParameterUtils() {}

    private static final int PAGE_NUM_DEFAULT = 1;

    private static final int PAGE_SIZE_DEFAULT = 100;

    public static Integer parsePageNumFromRequestOrAssignDefault(HttpServletRequest request) {
        return parsePageNumFromRequestOrAssignDefault(request, PAGE_NUM_DEFAULT);
    }

    public static Integer parsePageNumFromRequestOrAssignDefault(HttpServletRequest request, int defaultPageNum) {
        String pageNumParam = request.getParameter("pageNum");
        int pageNum;
        try {
            if (StringUtils.isNotEmpty(pageNumParam)) {
                pageNum = Integer.parseInt(pageNumParam);
            } else {
                pageNum = defaultPageNum;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse query parameter: pageNum");
        }
        return pageNum;
    }

    public static Integer parsePageSizeFromRequestOrAssignDefault(HttpServletRequest request) {
        return parsePageSizeFromRequestOrAssignDefault(request, PAGE_SIZE_DEFAULT);
    }

    public static Integer parsePageSizeFromRequestOrAssignDefault(HttpServletRequest request, int defaultPageSize) {
        String pageSizeParam = request.getParameter("pageSize");
        int pageSize;
        try {
            if (StringUtils.isNotEmpty(pageSizeParam)) {
                pageSize = Integer.parseInt(pageSizeParam);
            } else {
                pageSize = defaultPageSize;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse query parameter: pageSize");
        }
        return pageSize;
    }
}
