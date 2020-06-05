package com.miotech.kun.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.miotech.kun.workflow.web.mock.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.Assert.*;


public class DispatchServletTest extends GuiceTestBase {

    private DispatchServlet dispatchServlet;

    private MockController mockController = mock(MockController.class);

    private HttpServletResponse response = mock(HttpServletResponse.class);

    @Before
    public void injectRouter() {
        super.initInject();

        HttpRouter httpRouter = injector.getInstance(HttpRouter.class);
        httpRouter.addRouter(MockController.class);
        dispatchServlet = injector.getInstance(DispatchServlet.class);
    }

    @Test
    public void service_withRequest() throws IOException {
        HttpServletRequest request;
        request = new MockHttpServletRequest("GET", "/test");
        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .get();

        request = new MockHttpServletRequest("PUT", "/test");
        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .put();

        request = new MockHttpServletRequest("DELETE", "/test");
        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .delete();

        request = new MockHttpServletRequest("POST", "/test");
        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .post();
    }

    @Test
    public void service_withRequestBody() throws IOException {
        MockHttpServletRequest request;

        request = new MockHttpServletRequest("POST", "/test/_create");

        MockCreation mockCreation = MockCreationFactory.createMockObject();
        request.setContentAsString(JSONUtils.toJsonString(mockCreation));
        dispatchServlet.service(request, response);

        ArgumentCaptor<MockCreation> argument = ArgumentCaptor.forClass(MockCreation.class);
        Mockito.verify(mockController)
                .postWithRequestBody(argument.capture());
        assertEquals(mockCreation.getId(), argument.getValue().getId());
        assertEquals(mockCreation.getName(), argument.getValue().getName());
    }

    @Test
    public void service_withQueryParameter() throws IOException {
        MockHttpServletRequest request ;
        request = new MockHttpServletRequest("GET", "/test/query");
        request.setParameter("id", "1");
        request.setParameter("idx", "2");

        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .getWithQueryParameter("1", 2);
    }

    @Test
    public void service_withRouteVariable() throws IOException {
        HttpServletRequest request;

        request = new MockHttpServletRequest("GET", "/test/1");
        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .getWithRouteVariable("1");

        request = new MockHttpServletRequest("GET", "/test/1/int");
        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .getWithRouteVariableInt(1);

        request = new MockHttpServletRequest("GET", "/test/1/long");
        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .getWithRouteVariableLong(1L);

        request = new MockHttpServletRequest("GET", "/test/1/2/detail");
        dispatchServlet.service(request, response);
        Mockito.verify(mockController)
                .getWithMultiplePathVariable("1", "2");
    }

    @Test
    public void service_withNotFound() throws IOException {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/test/not/found");
        MockHttpServletResponse response = new MockHttpServletResponse();

        dispatchServlet.service(request, response);
        ObjectNode json = JSONUtils.jsonToObject(response.getContentAsString(), ObjectNode.class);
        json.remove("timestamp");
        assertEquals("{\"message\":\"Cannot resolve url mapping for: /test/not/found\",\"error\":\"Resource Not Found\",\"status\":400,\"path\":\"/test/not/found\"}",
                json.toString());
    }

    @Test
    public void service_withInternalError() throws IOException {
        HttpServletRequest request;
        MockHttpServletResponse response;

        request = new MockHttpServletRequest("GET", "/test/int");
        response = new MockHttpServletResponse();
        Mockito.when(mockController.getWithInt())
                .thenThrow(new RuntimeException("Internal error"));

        dispatchServlet.service(request, response);
        ObjectNode json = JSONUtils.jsonToObject(response.getContentAsString(), ObjectNode.class);
        json.remove("timestamp");
        assertEquals("{\"message\":\"Internal error\",\"error\":\"Internal Server Error\",\"status\":500,\"path\":\"/test/int\"}",
                json.toString());

        request = new MockHttpServletRequest("POST", "/test/int");
        Mockito.when(mockController.postWithInt())
                .thenThrow(new IllegalArgumentException("Illegal argument error"));

        response = new MockHttpServletResponse();
        dispatchServlet.service(request, response);
        json = JSONUtils.jsonToObject(response.getContentAsString(), ObjectNode.class);
        assertEquals("{\"code\":400,\"message\":\"Illegal argument error\"}",
                json.toString());
    }

    @Test
    public void service_withResponseJson() throws IOException {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/test/_response/json");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockCreation mockCreation = MockCreationFactory.createMockObject(1, "2");
        Mockito.when(mockController.returnWithRequestJson())
                .thenReturn(mockCreation);
        dispatchServlet.service(request, response);

        String content = response.getContentAsString();
        assertEquals("{\"id\":1,\"name\":\"2\"}",
                content);
    }
}