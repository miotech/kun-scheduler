package com.miotech.kun.workflow.web;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseSetup;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.commons.web.KunWebServer;
import com.miotech.kun.commons.web.module.CommonModule;
import com.miotech.kun.commons.web.module.KunWebServerModule;
import com.miotech.kun.workflow.SchedulerManager;
import com.miotech.kun.workflow.SchedulerModule;
import com.miotech.kun.workflow.core.publish.KafkaModule;
import com.miotech.kun.workflow.web.service.InitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@Singleton
public class KunWorkflowWebServer {
    private static final Logger logger = LoggerFactory.getLogger(KunWorkflowWebServer.class);

    @Inject
    private KunWebServer server;
    @Inject
    private Props props;
    //    @Inject
    private DataSource dataSource;
    @Inject
    private SchedulerManager schedulerManager;
    @Inject
    private InitService initService;

    public static void configureDB(Injector injector, Props props) {
        DataSource dataSource = injector.getInstance(DataSource.class);
        DatabaseSetup setup = new DatabaseSetup(dataSource, props);
        setup.start();
    }

    public void start() {
        initService.publishRpcServices();
        schedulerManager.start();
        this.server.start();
    }

    public void shutdown() {
        this.server.shutdown();
    }

    public boolean isReady() {
        return this.server.isServerRunning();
    }

    public static void main(final String[] args) {
        // Redirect all std out and err messages into log4j

        logger.info("Starting Jetty Kun Web Server...");

        /* Initialize Guice Injector */
        Props props = PropsUtils.loadAppProps();
        final Injector injector = Guice.createInjector(
                new KunWebServerModule(props),
                new KunWorkflowServerModule(props),
                new CommonModule(props),
                new RedisModule(props),
                new SchedulerModule()
        );
        configureDB(injector, props);
        launch(injector.getInstance(KunWorkflowWebServer.class));
    }

    private static void launch(final KunWorkflowWebServer webServer) {
        /* This creates the Web Server instance */
        webServer.start();
    }
}
