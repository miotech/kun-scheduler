package com.miotech.kun.commons.web.mock;

import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;

import java.util.List;

/**
 * Only for test usage
 */
@Singleton
public class MockController {

    @RouteMapping(url = "/test", method = "GET")
    public void get() { }

    @RouteMapping(url = "/test", method = "POST")
    public void post() { }

    @RouteMapping(url = "/test", method = "PUT")
    public void put() { }

    @RouteMapping(url = "/test", method = "DELETE")
    public void delete() { }

    @RouteMapping(url = "/test/int", method = "GET")
    public int getWithInt() { return 1; }

    @RouteMapping(url = "/test/int", method = "POST")
    public int postWithInt() { return 1; }

    @RouteMapping(url = "/test/_create", method = "POST")
    public void postWithRequestBody(@RequestBody MockCreation creation) { }

    @RouteMapping(url = "/test/_response/json", method = "GET")
    public MockCreation returnWithRequestJson() {
        return null;
    }

    @RouteMapping(url = "/test/query", method = "GET")
    public void getWithQueryParameter(@QueryParameter(defaultValue = "0") String id,
                                      @QueryParameter(name = "idx", defaultValue = "0") int id2
                                      ) { }

    @RouteMapping(url = "/test/queryList", method = "GET")
    public void getWithListQueryParameter(@QueryParameter(name = "ids") List<Long> ids
    ) { }

    @RouteMapping(url = "/test/{id}", method = "GET")
    public void getWithRouteVariable(@RouteVariable String id) { }

    @RouteMapping(url = "/test/{id}/int", method = "GET")
    public void getWithRouteVariableInt(@RouteVariable int id) { }

    @RouteMapping(url = "/test/{id}/long", method = "GET")
    public void getWithRouteVariableLong(@RouteVariable long id) { }

    @RouteMapping(url = "/test/{id}/{id2}/detail", method = "GET")
    public void getWithMultiplePathVariable(@RouteVariable String id, @RouteVariable String id2) { }
}
