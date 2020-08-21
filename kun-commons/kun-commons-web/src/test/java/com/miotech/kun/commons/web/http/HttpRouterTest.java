package com.miotech.kun.commons.web.http;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.mock.MockController;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpRouterTest extends GuiceTestBase {

    @Inject
    private HttpRouter httpRouter;

    private HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    public void getRequestMappingHandler() {
        httpRouter.addRouter(MockController.class);
        HttpRequestMappingHandler handler;

        HttpRoute getRoute = new HttpRoute("/test", HttpMethod.GET);
        mockRequest(getRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertEquals(getRoute, handler.getRoute());

        HttpRoute postRoute = new HttpRoute("/test", HttpMethod.POST);
        mockRequest(postRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertEquals(postRoute, handler.getRoute());

        HttpRoute putRoute = new HttpRoute("/test", HttpMethod.PUT);
        mockRequest(putRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertEquals(putRoute, handler.getRoute());

        HttpRoute deleteRoute = new HttpRoute("/test", HttpMethod.DELETE);
        mockRequest(deleteRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertEquals(deleteRoute, handler.getRoute());
    }

    @Test(expected = IllegalStateException.class)
    public void getRequestMappingHandler_withDuplicateRouteMapping() {
        httpRouter.addRouter(TestDuplicateRoutes.class);
    }

    @Test
    public void getRequestMappingHandler_withMalFormatted() {
        httpRouter.addRouter(MockController.class);
        HttpRequestMappingHandler handler;
        HttpRoute getRoute;
        HttpRoute requestRoute = new HttpRoute("/test", HttpMethod.GET);

        // "/" inside path should be empty
        getRoute = new HttpRoute("//test", HttpMethod.GET);
        mockRequest(getRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertNull( handler );

        // tailing "/"  should be valid
        getRoute = new HttpRoute("/test/", HttpMethod.GET);
        mockRequest(getRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertEquals(requestRoute, handler.getRoute());

        // double tailing "/"  should be empty
        getRoute = new HttpRoute("/test//", HttpMethod.GET);
        mockRequest(getRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertNull( handler );

        // tailing "?"  should be valid
        getRoute = new HttpRoute("/test?x=y", HttpMethod.GET);
        mockRequest(getRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertEquals(requestRoute, handler.getRoute());
    }

    @Test
    public void getRequestMappingHandler_withPathVariable() {
        httpRouter.addRouter(MockController.class);
        HttpRequestMappingHandler handler;
        HttpRoute getRoute;
        HttpRoute requestRoute;

        // single path variable
        requestRoute = new HttpRoute("/test/{id}", HttpMethod.GET);
        getRoute = new HttpRoute("/test/1", HttpMethod.GET);
        mockRequest(getRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertEquals(requestRoute, handler.getRoute());
        assertEquals("1", handler.getHttpRequest().getPathVariables().get("id"));

        // multiple path variables
        requestRoute = new HttpRoute("/test/{id}/{id2}/detail", HttpMethod.GET);
        getRoute = new HttpRoute("/test/1/2/detail", HttpMethod.GET);
        mockRequest(getRoute);
        handler = httpRouter.getRequestMappingHandler(request);
        assertEquals(requestRoute, handler.getRoute());
        assertEquals("1", handler.getHttpRequest().getPathVariables().get("id"));
        assertEquals("2", handler.getHttpRequest().getPathVariables().get("id2"));
    }

    /**
     * {@link HttpRouter#getRequestMappingHandler } will call getMethod and getRequestURI
     *   And
     * {@link HttpServletRequest#getRequestURI} return without query part
     */
    private void mockRequest(HttpRoute route) {
        Mockito.when(request.getMethod())
                .thenReturn(route.getMethod().toString());
        String url = route.getUrl()
                .replaceAll("\\?.*$", "");

        Mockito.when(request.getRequestURI())
                .thenReturn(url);
    }

    private static class TestDuplicateRoutes {

        @RouteMapping(url = "/test", method = "GET")
        public void get1() { }

        @RouteMapping(url = "/test", method = "GET")
        public void get2() { }
    }
}