package com.miotech.kun.dataplatform;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.RequestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/test-only")
public class TestOnlyController {
    @GetMapping("/success")
    public RequestResult<String> successGet() {
        return RequestResult.success("Good to go");
    }

    @GetMapping("/illegal-argument")
    public RequestResult<String> illegalArgumentExample() {
        throw new IllegalArgumentException("This is an example illegal argument failure");
    }

    @GetMapping("/illegal-state")
    public RequestResult<String> illegalStateExample() {
        throw new IllegalStateException("This is an example illegal state failure");
    }

    @GetMapping("/not-found")
    public RequestResult<String> notFoundExample() {
        throw new NoSuchElementException("This is an example not-found failure");
    }

    @GetMapping("/npe-user-defined")
    public RequestResult<String>  userDefinedNPEExample() {
        Preconditions.checkNotNull(null, "This is an example precondition check NPE failure");
        return RequestResult.success("You are not expected to see this");
    }

    @GetMapping("/npe-internal")
    public RequestResult<String> internalNPEExample() {
        throw new NullPointerException("This is an example internal NPE failure");
    }

    @GetMapping("/undefined-exception")
    public RequestResult<String> undefinedExceptionExample() {
        throw new UnknownTestException();
    }

    public static class UnknownTestException extends RuntimeException {
        @Override
        public String getMessage() {
            return "This is an example unknown exception";
        }
    }
}
