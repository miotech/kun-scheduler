package com.miotech.kun.commons.testing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.concurrent.TimeUnit;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

public class MockServerTestBase {

    private static ClientAndServer mockServer;

    @BeforeClass
    public static void setUp() {
        mockServer = startClientAndServer(10180);
    }

    @AfterClass
    public static void tearDown() {
        mockServer.stop();
    }

    public String getAddress() {
        return "http://localhost:" + mockServer.getPort();
    }

    public void mockPost(String url, String payload, String response) {
        mockPost(url, payload, response, 201);
    }

    public void mockPost(String url, String payload, String response, int statusCode) {
        mockRequest("POST", url, payload, statusCode, response);
    }

    public void mockPut(String url, String payload, String response) {
        mockPut(url, payload, response, 201);
    }

    public void mockPut(String url, String payload, String response, int statusCode) {
        mockRequest("PUT", url, payload, statusCode, response);
    }

    public void mockGet(String url, String response) {
        mockGet(url, response, 200);
    }

    public void mockGet(String url, String response, int statusCode) {
        mockRequest("GET", url, null, statusCode, response);
    }

    public void mockDelete(String url, String response) {
        mockDelete(url, response,200);
    }

    public void mockDelete(String url, String response, int statusCode) {
        mockRequest("DELETE", url, null, statusCode, response);
    }

    public void mockRequest(String method, String url, String payload, int statusCode, String response) {
        Header contentHeader = new Header("Content-Type", "application/json; charset=utf-8");
        String[] pieces = url.split("\\?");
        HttpRequest request = request()
                .withMethod(method)
                .withHeaders(contentHeader)
                .withPath(pieces[0]);
        if (pieces.length > 1) {
            String[] params = pieces[1].split("&");
            for(String param: params) {
                String[] pairs = param.split("=");
                request.withQueryStringParameter(pairs[0], pairs[1]);
            }
        }
        if (payload != null) {
            request.withBody(exact(payload));
        }
        HttpResponse resp = response()
                .withStatusCode(statusCode)
                .withHeaders(
                        contentHeader,
                        new Header("Cache-Control", "public, max-age=86400"))
                .withBody(response)
                .withDelay(TimeUnit.SECONDS, 1);

        mockServer
                .when(request, exactly(1))
                .respond(resp);
    }

}

