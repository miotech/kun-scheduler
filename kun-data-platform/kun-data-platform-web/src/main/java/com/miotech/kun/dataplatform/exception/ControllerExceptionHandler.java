package com.miotech.kun.dataplatform.exception;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.workflow.client.WorkflowApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.NoSuchElementException;
import java.util.Objects;

@SuppressWarnings("ThrowableInFormattedMessage")
@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    @ExceptionHandler(value
            = { IllegalArgumentException.class,
            WorkflowApiException.class})
    protected ResponseEntity<Object> handleBadRequest(
            RuntimeException ex, WebRequest request) {
        log.error("[HTTP 400] Bad request exception found when request = {}; params = {}.", request, request.getParameterMap(), ex);
        return ResponseEntity.badRequest()
                .body(RequestResult.error(400, ex.getMessage()));
    }

    // The HTTP 409 Conflict response status code indicates a request conflict with current state of the target resource.
    // See: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/409
    @ExceptionHandler(value = { IllegalStateException.class })
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        log.error("[HTTP 409] Illegal/Conflict state exception found when request = {}; params = {}.", request, request.getParameterMap(), ex);
        return ResponseEntity.status(409).body(RequestResult.error(409, ex.getMessage()));
    }

    @ExceptionHandler(value = { NoSuchElementException.class })
    protected  ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        log.error("[HTTP 404] Exception found when request = {}; params = {}.", request, request.getParameterMap(), ex);
        return ResponseEntity.status(404)
                .body(RequestResult.error(404, ex.getMessage()));
    }

    @ExceptionHandler(value = { NullPointerException.class })
    protected ResponseEntity<Object> handleNullPointer(RuntimeException ex, WebRequest request) {
        // Does this NPE comes from user-defined precondition check code?
        if (nullPtrExceptionComesFromPreconditionCheck(ex)) {
            log.error("[HTTP 400] Precondition check not null failed. Request = {}; params = {}.", request, request.getParameterMap(), ex);
            return ResponseEntity.badRequest()
                    .body(RequestResult.error(400, ex.getMessage()));
        } else {
            // if not, this should be an internal error
            log.error("[HTTP 500] Internal server error on NullPointerException. Request = {}; params = {}.", request, request.getParameterMap(), ex);
            return ResponseEntity
                    .status(500)
                    .body(RequestResult.error(500, ex.getMessage()));
        }
    }

    private boolean nullPtrExceptionComesFromPreconditionCheck(RuntimeException ex) {
        return (ex.getStackTrace().length > 0) &&
                Objects.equals(ex.getStackTrace()[0].getClassName(), Preconditions.class.getName());
    }

    /**
     * The final exception handler which if not matching above handlers
     */
    @ExceptionHandler(value = { Throwable.class })
    protected  ResponseEntity<Object> handleExceptionsNotMatched(Throwable ex, WebRequest request) {
        log.error("[HTTP 500] Internal server error on unknown exception. Request = {}; params = {}.", request, request.getParameterMap(), ex);
        return ResponseEntity.status(500).body(RequestResult.error(500, ex.getMessage()));
    }
}
