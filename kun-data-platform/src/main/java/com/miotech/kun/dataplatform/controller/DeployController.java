package com.miotech.kun.dataplatform.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.common.deploy.service.DeployService;
import com.miotech.kun.dataplatform.common.deploy.vo.DeployRequest;
import com.miotech.kun.dataplatform.common.deploy.vo.DeploySearchRequest;
import com.miotech.kun.dataplatform.common.deploy.vo.DeployVO;
import com.miotech.kun.dataplatform.model.deploy.Deploy;
import com.miotech.kun.workflow.client.model.PaginationResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@Api(tags = "Deploy")
@Slf4j
public class DeployController {

    @Autowired
    private DeployService deployService;

    @PostMapping("/deploys")
    @ApiOperation("Create Deploy")
    public RequestResult<DeployVO> createDeploy(@RequestBody DeployRequest request) {
        Deploy deploy = deployService.create(request);
        return RequestResult.success(deployService.convertVO(deploy));
    }

    @GetMapping("/deploys")
    @ApiOperation("Search Deploys")
    public RequestResult<PaginationResult<DeployVO>> searchDeploys(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(required = false) List<Long> creatorIds,
            @RequestParam(required = false) Optional<OffsetDateTime> submittedAtFrom,
            @RequestParam(required = false) Optional<OffsetDateTime> submittedAtTo,
            @RequestParam(required = false) List<Long> deployerIds,
            @RequestParam(required = false) Optional<OffsetDateTime> deployedAtFrom,
            @RequestParam(required = false) Optional<OffsetDateTime> deployedAtTo

    ) {
        DeploySearchRequest deploySearchRequest = new DeploySearchRequest(
                pageSize,
                pageNum,
                creatorIds,
                submittedAtFrom,
                submittedAtTo,
                deployerIds,
                deployedAtFrom,
                deployedAtTo
        );
        PaginationResult<Deploy> deploys = deployService.search(deploySearchRequest);
        PaginationResult<DeployVO> result = new PaginationResult<>(
                deploys.getPageSize(),
                deploys.getPageNum(),
                deploys.getTotalCount(),
                deploys.getRecords().stream()
                        .map(deployService::convertVO)
                        .collect(Collectors.toList())
        );
        return RequestResult.success(result);
    }

    @GetMapping("/deploys/{deployId}")
    @ApiOperation("Get Deploy")
    public RequestResult<DeployVO> getDeploy(@PathVariable Long deployId) {
        Deploy deploy = deployService.find(deployId);
        return RequestResult.success(deployService.convertVO(deploy));
    }

    @PostMapping("/deploys/{deployId}/_publish")
    @ApiOperation("Publish Deploy")
    public RequestResult<DeployVO> publishDeploy(@PathVariable Long deployId) {
        Deploy deploy = deployService.publish(deployId);
        return RequestResult.success(deployService.convertVO(deploy));
    }

}
