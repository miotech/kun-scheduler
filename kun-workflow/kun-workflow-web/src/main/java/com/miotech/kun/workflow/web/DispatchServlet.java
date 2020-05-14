package com.miotech.kun.workflow.web;

import com.miotech.kun.workflow.web.http.HttpAction;
import com.miotech.kun.workflow.web.http.HttpMethod;
import com.miotech.kun.workflow.web.http.HttpRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DispatchServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(DispatchServlet.class);

    private HttpRouter router;

    @Override
    public void init() {
        try {
            router = new HttpRouter();
            String basePackageName = this.getClass().getPackage().getName();
            router.scanPackage(basePackageName + ".controller");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doAction(req, resp, HttpMethod.POST);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doAction(req, resp, HttpMethod.GET);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doAction(req, resp, HttpMethod.DELETE);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doAction(req, resp, HttpMethod.PUT);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doAction(req, resp, HttpMethod.HEAD);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doAction(req, resp, HttpMethod.TRACE);
    }

    private void doAction(final HttpServletRequest req,
                          final HttpServletResponse resp,
                          HttpMethod method)
            throws ServletException, IOException {
        logger.trace("Receive request {} {}", method.toString(), req.getRequestURL());
        HttpRoute route = new HttpRoute(req.getRequestURI(), method);
        HttpAction action = router.getRoute(route);
        if (action != null) {
            List<Object> args = new ArrayList<>();
            if (action.getNeedInjectionRequest()) {
                args.add(req);
            }
            if (action.getNeedInjectionResp()) {
                args.add(resp);
            }
            action.call(args.toArray());
        } else {
            // respond with resource not found
            logger.debug("Cannot resolve url mapping for {}", req.getRequestURI());
        }
    }
}
