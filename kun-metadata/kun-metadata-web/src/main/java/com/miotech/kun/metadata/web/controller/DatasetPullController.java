package com.miotech.kun.metadata.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.model.vo.PullProcessVO;
import com.miotech.kun.metadata.web.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class DatasetPullController {
    private static final Logger logger = LoggerFactory.getLogger(DatasetPullController.class);

    @Inject
    private ProcessService processService;

    @RouteMapping(url = "/datasets/{gid}/_pull", method = "POST")
    public PullProcessVO pull(@RouteVariable Long gid) {
        logger.debug("DatasetPullController pull received gid: {}", gid);
        Preconditions.checkNotNull(gid, "Invalid parameter `gid`: found null object");
        return processService.submitPull(gid, DataBuilderDeployMode.DATASET);
    }

    @RouteMapping(url = "/datasets/{gid}/_pull/latest", method = "GET")
    public PullProcessVO getLatestPullProcess(@RouteVariable Long gid) {
        logger.debug("Fetching latest pull process for dataset: {}", gid);
        Preconditions.checkNotNull(gid, "Invalid parameter `gid`: null");
        Optional<PullProcessVO> latestProcess = processService.fetchLatestProcessByDatasetId(gid);
        return latestProcess.orElse(null);
    }
}
