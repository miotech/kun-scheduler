package com.miotech.kun.workflow.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.variable.service.VariableService;
import com.miotech.kun.workflow.common.variable.vo.VariableVO;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;


@Singleton
public class VariableController {
    private final Logger logger = LoggerFactory.getLogger(VariableController.class);

    @Inject
    private VariableService variableService;

    @RouteMapping(url= "/variables", method = "GET")
    public Object getAllVariables() {
        return variableService.findAll()
                .stream()
                .map(variableService::convertVO)
                .collect(Collectors.toList());
    }

    @RouteMapping(url= "/variables", method = "POST")
    public Object createVariable(@RequestBody VariableVO variableVO) {
        Preconditions.checkNotNull(variableVO, "Received invalid variable properties: null");
        logger.debug("variableVO = {}", variableVO);
        return variableService.convertVO(variableService.createVariable(variableVO));
    }

    @RouteMapping(url= "/variables", method = "PUT")
    public Object updateVariable(@RequestBody VariableVO variableVO) {
        Preconditions.checkNotNull(variableVO, "Received invalid variable properties: null");
        logger.debug("variableVO = {}", variableVO);
        return variableService.convertVO(variableService.updateVariable(variableVO));
    }

    @RouteMapping(url= "/variables/{key}", method = "GET")
    public Object getVariable(@RouteVariable String key) {
        return variableService.convertVO(variableService.find(key));
    }
}
