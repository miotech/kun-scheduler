package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.commons.db.sql.SortOrder;
import com.miotech.kun.dataplatform.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.common.taskdefview.service.TaskDefinitionViewService;
import com.miotech.kun.dataplatform.common.taskdefview.vo.CreateTaskDefViewRequest;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.common.taskdefview.vo.UpdateTaskDefViewRequest;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/")
@Api(tags = "TaskDefinitionView")
@Slf4j
public class TaskDefinitionViewController {
    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private TaskDefinitionViewService taskDefinitionViewService;

    @GetMapping("/task-def-views")
    @ApiOperation("Search task definition view")
    public RequestResult<PageResult<TaskDefinitionViewVO>> searchTaskDefinitionView(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(defaultValue = "id") String sortKey,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long creator
    ) {
        TaskDefinitionViewSearchParams params = TaskDefinitionViewSearchParams.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .keyword(keyword)
                .creator(creator)
                .sortOrder(SortOrder.from(sortOrder))
                .sortKey(TaskDefinitionViewSearchParams.SortKey.from(sortKey))
                .build();
        PageResult<TaskDefinitionViewVO> viewVOPageResult = taskDefinitionViewService.searchPage(params);
        return RequestResult.success(viewVOPageResult);
    }

    @GetMapping("/task-def-views/{viewId}")
    @ApiOperation("Get task definition view detailed information")
    public RequestResult<TaskDefinitionView> getTaskDefinitionViewDetail(@PathVariable Long viewId) {
        Optional<TaskDefinitionView> taskDefinitionView = taskDefinitionViewService.fetchById(viewId);
        if (taskDefinitionView.isPresent()) {
            return RequestResult.success(taskDefinitionView.get());
        }
        // else
        return RequestResult.error(404, String.format("Cannot find task definition view with id: %s", viewId));
    }

    @PostMapping("/task-def-views")
    @ApiOperation("Create a task definition view")
    public RequestResult<TaskDefinitionView> createTaskDefinitionView(
            @RequestBody CreateTaskDefViewRequest createRequest
    ) {
        TaskDefinitionView view = taskDefinitionViewService.create(createRequest);
        return RequestResult.success(view);
    }

    @PutMapping("/task-def-views/{viewId}")
    @ApiOperation("Update a task definition view")
    public RequestResult<TaskDefinitionView> updateTaskDefinitionView(
            @PathVariable Long viewId,
            @RequestBody UpdateTaskDefViewRequest updateRequest
    ) {
        TaskDefinitionView updatedView = taskDefinitionViewService.update(viewId, updateRequest);
        return RequestResult.success(updatedView);
    }

    @DeleteMapping("/task-def-views/{viewId}")
    @ApiOperation("Delete a task definition view")
    public RequestResult<Object> deleteTaskDefinitionView(@PathVariable Long viewId) {
        taskDefinitionViewService.deleteById(viewId);
        return RequestResult.success();
    }
}
