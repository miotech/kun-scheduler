package com.miotech.kun.metadata.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.metadata.web.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProcessController {
    private static Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Inject
    private ProcessService processService;

    @RouteMapping(url = "/process/{processId}", method = "GET")
    public Object getProcessStatus(@RouteVariable String processId) {
        logger.debug("ProcessController getProcessStatus received id: {}", processId);
        Preconditions.checkNotNull(processId, "Invalid parameter `id`: found null object");
        return processService.fetchStatus(processId);
    }

}
