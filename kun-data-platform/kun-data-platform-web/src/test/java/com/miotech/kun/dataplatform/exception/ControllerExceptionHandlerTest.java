package com.miotech.kun.dataplatform.exception;

import com.miotech.kun.dataplatform.AppTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ControllerExceptionHandlerTest extends AppTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void controllerExceptionHandler_shouldNotIntercept_whenNoExceptionThrown() throws Exception {
        mockMvc.perform(get("/test-only/success")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"code\":0,\"note\":\"Operation Successful\",\"result\":\"Good to go\"}"));
    }

    @Test
    public void controllerExceptionHandler_shouldResponse404_whenNoSuchElementExceptionThrown() throws Exception {
        mockMvc.perform(get("/test-only/not-found")).andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"code\":404,\"note\":\"This is an example not-found failure\"}"));
    }

    @Test
    public void controllerExceptionHandler_shouldResponse400_whenIllegalArgumentExceptionThrown() throws Exception {
        mockMvc.perform(get("/test-only/illegal-argument")).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"code\":400,\"note\":\"This is an example illegal argument failure\"}"));
    }

    @Test
    public void controllerExceptionHandler_shouldResponse409_whenIllegalStateExceptionThrown() throws Exception {
        mockMvc.perform(get("/test-only/illegal-state")).andDo(print())
                .andExpect(status().is(409))
                .andExpect(content().string("{\"code\":409,\"note\":\"This is an example illegal state failure\"}"));
    }

    @Test
    public void controllerExceptionHandler_shouldResponse400_whenUserDefinedNPEThrown() throws Exception {
        mockMvc.perform(get("/test-only/npe-user-defined")).andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"code\":400,\"note\":\"This is an example precondition check NPE failure\"}"));
    }

    @Test
    public void controllerExceptionHandler_shouldResponse500_whenInternalNPEThrown() throws Exception {
        mockMvc.perform(get("/test-only/npe-internal")).andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"code\":500,\"note\":\"This is an example internal NPE failure\"}"));
    }

    @Test
    public void controllerExceptionHandler_shouldResponse500_whenUnpredictedExceptionTypeThrown() throws Exception {
        mockMvc.perform(get("/test-only/undefined-exception")).andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"code\":500,\"note\":\"This is an example unknown exception\"}"));
    }
}