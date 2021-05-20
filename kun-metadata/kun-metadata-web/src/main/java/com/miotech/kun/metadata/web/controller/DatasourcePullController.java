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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class DatasourcePullController {

    private static Logger logger = LoggerFactory.getLogger(DatasetPullController.class);

    @Inject
    private ProcessService processService;

    @RouteMapping(url = "/datasources/{id}/_pull", method = "POST")
    public Object pull(@RouteVariable Long id) {
        logger.debug("DatasetPullController pull received id: {}", id);
        Preconditions.checkNotNull(id, "Invalid parameter `id`: found null object");
        PullProcessVO pullProcessVO = processService.submitPull(id, DataBuilderDeployMode.DATASOURCE);
        return pullProcessVO;
    }

    @RouteMapping(url = "/datasources/_pull/latest", method = "GET")
    public Map<String, PullProcessVO> getDataSourcesPullProcesses(@RouteVariable List<Long> dataSourceIds) {
        logger.debug("Fetching pull progress for each datasource...");
        Map<Long, PullProcessVO> latestProcesses = processService.fetchLatestProcessByDataSourceIds(dataSourceIds);
        Map<String, PullProcessVO> result = new HashMap<>();
        for (Map.Entry entry : latestProcesses.entrySet()) {
            result.put(entry.getKey().toString(), (PullProcessVO) entry.getValue());
        }
        return result;
    }
}
