package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.common.taskdefview.service.TaskDefinitionViewService;
import com.miotech.kun.dataplatform.common.taskdefview.vo.CreateTaskDefViewRequest;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@Api(tags = "TaskDefinitionView")
@Slf4j
public class TaskDefinitionViewController {
    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private TaskDefinitionViewService taskDefinitionViewService;

    @PostMapping("/task-def-views")
    @ApiOperation("Create Task Definition View")
    public RequestResult<TaskDefinitionView> createTaskDefinitionView(@RequestBody CreateTaskDefViewRequest createRequest) {
        TaskDefinitionView view = taskDefinitionViewService.create(createRequest);
        return RequestResult.success(view);
    }

    @GetMapping("/task-def-view")
    @ApiOperation("Search Task Definition View")
    public RequestResult<PageResult<TaskDefinitionViewVO>> searchTaskDefinitionView(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(defaultValue = "") String keyword
    ) {
        TaskDefinitionViewSearchParams params = TaskDefinitionViewSearchParams.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .keyword(keyword)
                .build();
        PageResult<TaskDefinitionViewVO> viewVOPageResult = taskDefinitionViewService.searchPage(params);
        return RequestResult.success(viewVOPageResult);
    }
}
