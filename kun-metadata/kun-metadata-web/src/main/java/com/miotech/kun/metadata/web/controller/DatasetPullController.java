package com.miotech.kun.metadata.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.model.vo.ProcessVO;
import com.miotech.kun.metadata.web.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DatasetPullController {
    private static Logger logger = LoggerFactory.getLogger(DatasetPullController.class);

    @Inject
    private ProcessService processService;

    @RouteMapping(url = "/datasets/{gid}/_pull", method = "POST")
    public Object pull(@RouteVariable Long gid) {
        logger.debug("DatasetPullController pull received gid: {}", gid);
        Preconditions.checkNotNull(gid, "Invalid parameter `gid`: found null object");
        return new ProcessVO(processService.submit(gid, DataBuilderDeployMode.DATASET));
    }

}
