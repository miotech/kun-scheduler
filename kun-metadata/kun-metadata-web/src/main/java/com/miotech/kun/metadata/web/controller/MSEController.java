package com.miotech.kun.metadata.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.metadata.core.model.event.MetadataStatisticsEvent;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import com.miotech.kun.metadata.web.service.MSEService;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MSEController {
    private static final Logger logger = LoggerFactory.getLogger(MSEController.class);

    private MSEService mseService;

    @Inject
    public MSEController(MSEService mseService) {
        this.mseService = mseService;
    }

    @RouteMapping(url = "/mse/_execute", method = "POST")
    public TaskRun execute(@RequestBody MetadataStatisticsEvent mse) {
        logger.info("Prepare to execute mse task: {}", JSONUtils.toJsonString(mse));
        return mseService.execute(mse);
    }

}
