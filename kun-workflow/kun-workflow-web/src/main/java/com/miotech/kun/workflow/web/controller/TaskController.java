package com.miotech.kun.workflow.web.controller;

import com.google.inject.Singleton;
import com.miotech.kun.workflow.web.annotation.RouteMapping;

import javax.servlet.http.HttpServletRequest;

@Singleton
public class TaskController {

    @RouteMapping(url= "/tasks", method = "GET")
    public Object getTask(HttpServletRequest request) {
        return null;
    }
}
