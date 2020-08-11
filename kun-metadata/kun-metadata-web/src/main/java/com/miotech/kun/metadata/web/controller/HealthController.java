package com.miotech.kun.metadata.web.controller;

import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.web.model.vo.AcknowledgementVO;

@Singleton
public class HealthController {

    @RouteMapping(url= "/health", method = "GET")
    public AcknowledgementVO healthOk() {
        return new AcknowledgementVO("Kun metadata api, Status is ok!");
    }

}
