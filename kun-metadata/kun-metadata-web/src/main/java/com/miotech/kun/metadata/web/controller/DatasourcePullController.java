package com.miotech.kun.metadata.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.model.vo.PullProcessVO;
import com.miotech.kun.metadata.web.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class DatasourcePullController {

    private static final Logger logger = LoggerFactory.getLogger(DatasourcePullController.class);

    @Inject
    private ProcessService processService;

    @RouteMapping(url = "/datasources/{id}/_pull", method = "POST")
    public Object pull(@RouteVariable Long id) {
        logger.debug("DatasetPullController pull received id: {}", id);
        Preconditions.checkNotNull(id, "Invalid parameter `id`: found null object");
        return processService.submitPull(id, DataBuilderDeployMode.DATASOURCE);
    }

    @RouteMapping(url = "/datasources/_pull/latest", method = "GET")
    public Map<String, PullProcessVO> getDataSourcesPullProcesses(@QueryParameter List<Long> dataSourceIds) {
        logger.debug("Fetching pull progress for each datasource...");
        Map<Long, PullProcessVO> latestProcesses = processService.fetchLatestProcessByDataSourceIds(dataSourceIds);
        Map<String, PullProcessVO> result = new HashMap<>();
        for (Map.Entry<Long, PullProcessVO> entry : latestProcesses.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue());
        }
        return result;
    }
}
