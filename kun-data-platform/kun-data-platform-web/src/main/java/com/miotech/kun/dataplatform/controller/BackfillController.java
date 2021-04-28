package com.miotech.kun.dataplatform.controller;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.constant.ErrorCode;
import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.commons.db.sql.SortOrder;
import com.miotech.kun.dataplatform.common.backfill.service.BackfillService;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillCreateInfo;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillDetailVO;
import com.miotech.kun.dataplatform.common.backfill.vo.BackfillSearchParams;
import com.miotech.kun.dataplatform.constant.BackfillConstants;
import com.miotech.kun.dataplatform.exception.BackfillTooManyTasksException;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/")
@Api(tags = "Backfill")
@Slf4j
public class BackfillController {
    @Autowired
    private BackfillService backfillService;

    @Autowired
    private WorkflowClient workflowClient;

    @PostMapping("/backfills")
    @ApiOperation("Create and run data backfill")
    public RequestResult<Backfill> createAndRunBackfill(@RequestBody BackfillCreateInfo createInfo) {
        Preconditions.checkArgument(createInfo != null, "parameter `createInfo` should not be null");
        if (createInfo.getWorkflowTaskIds().size() > BackfillConstants.MAX_BACKFILL_TASKS) {
            throw new BackfillTooManyTasksException();
        }
        Backfill payload = backfillService.createAndRun(createInfo);
        return RequestResult.success(payload);
    }

    @GetMapping("/backfills")
    @ApiOperation("Get page of backfills")
    public RequestResult<PageResult<Backfill>> searchBackfills(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> creatorIds,
            @RequestParam(required = false) String timeRngStart,
            @RequestParam(required = false) String timeRngEnd,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "100") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortKey,
            @RequestParam(defaultValue = "DESC") String sortOrder
    ) {
        OffsetDateTime startAt = Objects.nonNull(timeRngStart) ? DateTimeUtils.fromISODateTimeString(timeRngStart) : null;
        OffsetDateTime endAt = Objects.nonNull(timeRngEnd) ? DateTimeUtils.fromISODateTimeString(timeRngEnd) : null;
        SortOrder sortOrderValue = SortOrder.from(sortOrder);

        BackfillSearchParams searchParams = new BackfillSearchParams();
        searchParams.setPageNumber(pageNum);
        searchParams.setPageSize(pageSize);
        searchParams.setSortKey(sortKey);
        searchParams.setSortOrder(sortOrderValue);
        if (Strings.isNotBlank(name)) {
            searchParams.setName(name);
        }
        if (Objects.nonNull(creatorIds) && creatorIds.size() > 0) {
            searchParams.setCreators(creatorIds);
        }
        if (Objects.nonNull(startAt)) {
            searchParams.setTimeRngStart(startAt);
        }
        if (Objects.nonNull(endAt)) {
            searchParams.setTimeRngEnd(endAt);
        }

        PageResult<Backfill> backfillPageResult = backfillService.search(searchParams);
        return RequestResult.success(backfillPageResult);
    }

    @GetMapping("/backfills/{backfillId}")
    @ApiOperation("Get backfill details")
    public RequestResult<BackfillDetailVO> fetchBackfillDetail(@PathVariable Long backfillId) {
        Preconditions.checkArgument(Objects.nonNull(backfillId), "backfill id cannot be null");

        Optional<Backfill> backfillOptional = backfillService.fetchById(backfillId);
        if (!backfillOptional.isPresent()) {
            return RequestResult.error(ErrorCode.FAILED.getCode(), String.format("backfill with id: %s does not exists", backfillId));
        }
        List<TaskRun> taskRuns = backfillService.fetchTaskRunsByBackfillId(backfillId);
        BackfillDetailVO backfillDetailVO = BackfillDetailVO.from(backfillOptional.get(), taskRuns);

        return RequestResult.success(backfillDetailVO);
    }

    @PostMapping("/backfills/{backfillId}/_run")
    @ApiOperation("Run backfill task runs immediately")
    public RequestResult<Object> runBackfill(@PathVariable Long backfillId) {
        Preconditions.checkArgument(Objects.nonNull(backfillId), "backfill id cannot be null");

        backfillService.runBackfillById(backfillId);

        return RequestResult.success(new AcknowledgementVO("Operation acknowledged."));
    }

    @DeleteMapping("/backfills/{backfillId}/_abort")
    @ApiOperation("Stop backfill task runs immediately")
    public RequestResult<Object> abortBackfills(@PathVariable Long backfillId) {
        Preconditions.checkArgument(Objects.nonNull(backfillId), "backfill id cannot be null");

        backfillService.stopBackfillById(backfillId);

        return RequestResult.success(new AcknowledgementVO("Operation acknowledged."));
    }

    @PostMapping("/backfills/taskruns/{taskRunId}/_restart")
    @ApiOperation("Rerun a single taskrun instance immediately")
    public RequestResult<TaskRun> restartTaskRunInstance(@PathVariable Long taskRunId) {
        Preconditions.checkArgument(Objects.nonNull(taskRunId), "task run id cannot be null");
        TaskRun taskRun = workflowClient.restartTaskRun(taskRunId);
        return RequestResult.success(taskRun);
    }

    @PutMapping("/backfills/taskruns/{taskRunId}/_abort")
    @ApiOperation("Abort a single taskrun instance immediately")
    public RequestResult<TaskRun> abortTaskRunInstance(@PathVariable Long taskRunId) {
        Preconditions.checkArgument(Objects.nonNull(taskRunId), "task run id cannot be null");
        TaskRun taskRun = workflowClient.stopTaskRun(taskRunId);
        return RequestResult.success(taskRun);
    }
}
