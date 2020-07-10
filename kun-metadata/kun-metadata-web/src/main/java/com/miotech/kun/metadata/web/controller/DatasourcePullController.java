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
public class DatasourcePullController {

    private static Logger logger = LoggerFactory.getLogger(DatasetPullController.class);

    @Inject
    private ProcessService processService;

    @RouteMapping(url = "/datasources/{id}/_pull", method = "POST")
    public Object pull(@RouteVariable Long id) {
        logger.debug("DatasetPullController pull received id: {}", id);
        Preconditions.checkNotNull(id, "Invalid parameter `id`: found null object");
        return new ProcessVO(processService.submit(id, DataBuilderDeployMode.DATASOURCE));
    }

}
