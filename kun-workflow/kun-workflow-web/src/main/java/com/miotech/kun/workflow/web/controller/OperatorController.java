package com.miotech.kun.workflow.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.operator.filter.OperatorSearchFilter;
import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.web.annotation.QueryParameter;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.entity.AcknowledgementVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class OperatorController {

    private final Logger logger = LoggerFactory.getLogger(OperatorController.class);

    private final OperatorService operatorService;

    @Inject
    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @RouteMapping(url= "/operators", method = "GET")
    public Object getOperators(@QueryParameter(defaultValue = "1") int pageNum,
                               @QueryParameter(defaultValue = "100") int pageSize,
                               @QueryParameter String name) {

        OperatorSearchFilter filter = OperatorSearchFilter.newBuilder()
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .withKeyword(name)
                .build();
        return operatorService.fetchOperatorsWithFilter(filter);
    }

    @RouteMapping(url= "/operators", method = "POST")
    public Object createOperator(@RequestBody OperatorPropsVO operatorPropsVO) {
        Preconditions.checkNotNull(operatorPropsVO, "Received invalid operator properties: null");
        logger.debug("operatorPropsVO = {}", operatorPropsVO);
        return operatorService.createOperator(operatorPropsVO);
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "DELETE")
    public Object deleteOperator(@RouteVariable Long operatorId) {
        operatorService.deleteOperatorById(operatorId);
        return new AcknowledgementVO("Delete success");
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "PUT")
    public Object updateOperator(@RouteVariable Long operatorId, @RequestBody OperatorPropsVO operatorPropsVO) {
        Preconditions.checkNotNull(operatorId, "Received invalid operator ID in path parameter.");
        return operatorService.fullUpdateOperator(operatorId, operatorPropsVO);
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "PATCH")
    public Object patchOperator(@RouteVariable Long operatorId, @RequestBody OperatorPropsVO operatorPropsVO) {
        Preconditions.checkNotNull(operatorId, "Received invalid operator ID in path parameter.");
        return operatorService.partialUpdateOperator(operatorId, operatorPropsVO);
    }
}
