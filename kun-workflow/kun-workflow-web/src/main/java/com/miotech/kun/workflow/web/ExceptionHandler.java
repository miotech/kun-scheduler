package com.miotech.kun.workflow.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.ResponseException;
import com.miotech.kun.commons.web.annotation.ResponseStatus;
import com.miotech.kun.workflow.common.exception.BadRequestException;
import com.miotech.kun.commons.web.serializer.JsonSerializer;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.exception.ExceptionResponse;

import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeParseException;

@Singleton
public class ExceptionHandler {
    @Inject
    private JsonSerializer jsonSerializer;

    @ResponseStatus(code = 400)
    @ResponseException(IllegalArgumentException.class)
    public void illegalArgHandler(HttpServletResponse resp,
                                   IllegalArgumentException e) {
        ExceptionResponse responseObj = new ExceptionResponse(400, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }

    @ResponseStatus(code = 404)
    @ResponseException(EntityNotFoundException.class)
    public void entityNotFoundHandler(HttpServletResponse resp,
                                   EntityNotFoundException e) {
        ExceptionResponse responseObj = new ExceptionResponse(404, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }


    @ResponseStatus(code = 400)
    @ResponseException(BadRequestException.class)
    public void badRequestHandler(HttpServletResponse resp,
                                   BadRequestException e) {
        ExceptionResponse responseObj = new ExceptionResponse(400, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }

    @ResponseStatus(code = 400)
    @ResponseException(DateTimeParseException.class)
    public void dateTimeParseExceptionHandler(HttpServletResponse resp,
                                               DateTimeParseException e) {
        ExceptionResponse responseObj = new ExceptionResponse(400, e.getMessage());
        jsonSerializer.writeResponseAsJson(resp, responseObj);
    }
}
