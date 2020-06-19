package com.miotech.kun.workflow.web.controller;

import com.google.inject.Singleton;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.entity.AcknowledgementVO;

@Singleton
public class HealthController {

    @RouteMapping(url= "/health", method = "GET")
    public AcknowledgementVO healthOk() {
        return new AcknowledgementVO("Kun workflow api, Status is ok!");
    }

}
