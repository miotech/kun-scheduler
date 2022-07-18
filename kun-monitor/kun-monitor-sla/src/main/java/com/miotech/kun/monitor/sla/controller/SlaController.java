package com.miotech.kun.monitor.sla.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.monitor.sla.common.service.SlaService;
import com.miotech.kun.monitor.sla.model.SlaBacktrackingInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Slf4j
public class SlaController {

    @Autowired
    private SlaService slaService;

    @GetMapping("/sla/backtracking/{taskDefinitionId}")
    public RequestResult<SlaBacktrackingInformation> getBacktrackingInformation(@PathVariable Long taskDefinitionId) {
        return RequestResult.success(slaService.findBacktrackingInformation(taskDefinitionId));
    }

}
