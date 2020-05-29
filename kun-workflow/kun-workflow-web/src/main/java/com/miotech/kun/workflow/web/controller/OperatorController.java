package com.miotech.kun.workflow.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.operator.filter.OperatorSearchFilter;
import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.entity.AcknowledgementVO;
import com.miotech.kun.workflow.web.utils.RequestQueryParameterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@Singleton
public class OperatorController {

    private final Logger logger = LoggerFactory.getLogger(OperatorController.class);

    private final OperatorService operatorService;

    @Inject
    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @RouteMapping(url= "/operators", method = "GET")
    public Object getOperators(HttpServletRequest request) {
        Integer pageNum = RequestQueryParameterUtils.parsePageNumFromRequestOrAssignDefault(request);
        Integer pageSize = RequestQueryParameterUtils.parsePageSizeFromRequestOrAssignDefault(request);

        return operatorService.fetchOperatorsWithFilter(
                OperatorSearchFilter.newBuilder()
                    .withPageNum(pageNum)
                    .withPageSize(pageSize)
                    .build()
        );
    }

    @RouteMapping(url= "/operators", method = "POST")
    public Object createOperator(@RequestBody OperatorPropsVO operatorPropsVO) {
        Preconditions.checkNotNull(operatorPropsVO, "Received invalid operator properties: null");
        logger.debug("operatorPropsVO = {}", operatorPropsVO);
        Operator operator = operatorService.createOperator(operatorPropsVO);
        return operator;
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "DELETE")
    public Object deleteOperator(@RouteVariable(required = true) Long operatorId) {
        operatorService.deleteOperatorById(operatorId);
        return new AcknowledgementVO();
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "PUT")
    public Object updateOperator(@RouteVariable(required = true) Long operatorId, @RequestBody OperatorPropsVO operatorPropsVO) {
        Preconditions.checkNotNull(operatorId, "Received invalid operator ID in path parameter.");
        return operatorService.fullUpdateOperator(operatorId, operatorPropsVO);
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "PATCH")
    public Object patchOperator(@RouteVariable(required = true) Long operatorId, @RequestBody OperatorPropsVO operatorPropsVO) {
        Preconditions.checkNotNull(operatorId, "Received invalid operator ID in path parameter.");
        return operatorService.partialUpdateOperator(operatorId, operatorPropsVO);
    }
}
