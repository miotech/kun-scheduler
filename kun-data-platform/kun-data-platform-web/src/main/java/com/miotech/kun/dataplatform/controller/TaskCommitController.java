package com.miotech.kun.dataplatform.controller;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.common.commit.service.TaskCommitService;
import com.miotech.kun.dataplatform.common.commit.vo.CommitRequest;
import com.miotech.kun.dataplatform.common.commit.vo.CommitSearchRequest;
import com.miotech.kun.dataplatform.common.commit.vo.TaskCommitVO;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.workflow.client.model.PaginationResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@Api(tags = "TaskCommit")
@Slf4j
public class TaskCommitController {

    @Autowired
    private TaskCommitService taskCommitService;

    @PostMapping("/task-commits")
    @ApiOperation("Create TaskCommit")
    public RequestResult<TaskCommitVO> createCommit(@RequestBody CommitRequest request) {
        Preconditions.checkNotNull(request);
        TaskCommit taskCommit = taskCommitService.commit(request.getDefinitionId(), request.getMessage());
        return RequestResult.success(taskCommitService.convertVO(taskCommit));
    }

    @GetMapping("/task-commits")
    @ApiOperation("Search TaskCommits")
    public RequestResult<PaginationResult<TaskCommitVO>> searchCommits(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(required = false) List<Long> committerIds,
            @RequestParam(required = false) List<Long> definitionIds,
            @RequestParam(required = false) Optional<Boolean> isLatest
    ) {
        CommitSearchRequest request = new CommitSearchRequest(
                pageSize,
                pageNum,
                committerIds,
                definitionIds,
                isLatest
        );
        PaginationResult<TaskCommit> taskCommit = taskCommitService.search(request);
        PaginationResult<TaskCommitVO> result = new PaginationResult<>(
                taskCommit.getPageSize(),
                taskCommit.getPageNum(),
                taskCommit.getTotalCount(),
                taskCommit.getRecords().stream()
                        .map(taskCommitService::convertVO)
                        .collect(Collectors.toList())
        );

        return RequestResult.success(result);
    }

    @GetMapping("/task-commits/{commitId}")
    @ApiOperation("Get TaskCommit")
    public RequestResult<TaskCommitVO> getCommit(@PathVariable Long commitId) {
        TaskCommit taskCommit = taskCommitService.find(commitId);
        return RequestResult.success(taskCommitService.convertVO(taskCommit));
    }
}
