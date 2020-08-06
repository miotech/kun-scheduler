package com.miotech.kun.dataplatform.exception;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.workflow.client.WorkflowApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(value
            = { IllegalArgumentException.class,
            IllegalStateException.class,
            WorkflowApiException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(RequestResult.error(400, ex.getMessage()));
    }

    @ExceptionHandler(value
            = { NullPointerException.class })
    protected ResponseEntity<Object> handleNullpointer(
            RuntimeException ex, WebRequest request) {
        if (ex.getStackTrace().length > 0
                && ex.getStackTrace()[0].getClassName()
                        .equals(Preconditions.class.getName())) {
            ;
            return ResponseEntity.badRequest()
                    .body(RequestResult.error(400, ex.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(RequestResult.error(500, ex.getMessage()));
        }

    }

}
