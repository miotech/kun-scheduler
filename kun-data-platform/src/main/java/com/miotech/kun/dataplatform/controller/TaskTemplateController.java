package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.common.tasktemplate.service.TaskTemplateService;
import com.miotech.kun.dataplatform.common.tasktemplate.vo.TaskTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("")
@Api(tags = "TaskTemplate")
@Slf4j
public class TaskTemplateController {

    @Autowired
    private TaskTemplateService taskTemplateService;

    @GetMapping("/task-templates")
    @ApiOperation("List TaskTemplate")
    public RequestResult<List<TaskTemplateVO>> searchTaskTemplates() {
        return RequestResult.success(
                taskTemplateService.getAllTaskTemplates()
                        .stream()
                        .map(taskTemplateService::convertToVO)
                        .collect(Collectors.toList()));
    }

}
