package com.miotech.kun.workflow.web.controller;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.workflow.common.operator.filter.OperatorSearchFilter;
import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.common.operator.vo.OperatorPropsVO;
import com.miotech.kun.workflow.web.entity.AcknowledgementVO;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Singleton
public class OperatorController {

    private final Logger logger = LoggerFactory.getLogger(OperatorController.class);
    private static final Long MAX_FILE_SIZE = 300 * 1000 * 1000L;
    private static final DiskFileItemFactory factory = new DiskFileItemFactory();

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
        return operatorService.convertOperatorToOperatorVO(operatorService.createOperator(operatorPropsVO));
    }

    @RouteMapping(url= "/operators/{id}/_upload", method = "POST")
    public AcknowledgementVO uploadResource(@RouteVariable Long id,
                                            HttpServletRequest httpRequest) {
        try {
            boolean isMultipart = ServletFileUpload.isMultipartContent(httpRequest);
            Preconditions.checkArgument(isMultipart, "Should contain valid upload file");
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(MAX_FILE_SIZE);
            List<FileItem> files = upload.parseRequest(httpRequest);
            operatorService.uploadOperatorJar(id, files);
            return new AcknowledgementVO("package uploaded");
        } catch ( FileUploadException e) {
            logger.error("Failed to upload:", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "DELETE")
    public Object deleteOperator(@RouteVariable Long operatorId) {
        operatorService.deleteOperatorById(operatorId);
        return new AcknowledgementVO("Delete success");
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "GET")
    public Object getOperator(@RouteVariable Long operatorId) {
        Preconditions.checkNotNull(operatorId, "Received invalid operator ID in path parameter.");
        return operatorService.convertOperatorToOperatorVO(operatorService.findOperator(operatorId));
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "PUT")
    public Object updateOperator(@RouteVariable Long operatorId, @RequestBody OperatorPropsVO operatorPropsVO) {
        Preconditions.checkNotNull(operatorId, "Received invalid operator ID in path parameter.");
        return operatorService.convertOperatorToOperatorVO(operatorService.fullUpdateOperator(operatorId, operatorPropsVO));
    }

    @RouteMapping(url= "/operators/{operatorId}", method = "PATCH")
    public Object patchOperator(@RouteVariable Long operatorId, @RequestBody OperatorPropsVO operatorPropsVO) {
        Preconditions.checkNotNull(operatorId, "Received invalid operator ID in path parameter.");
        return operatorService.convertOperatorToOperatorVO(operatorService.partialUpdateOperator(operatorId, operatorPropsVO));
    }
}
