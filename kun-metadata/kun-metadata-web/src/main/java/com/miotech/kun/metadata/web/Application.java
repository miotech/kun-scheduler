package com.miotech.kun.metadata.web;

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
import com.miotech.kun.metadata.web.kafka.MetadataConsumerStarter;
import com.miotech.kun.metadata.web.service.InitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@Singleton
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final Props props;
    private final DataSource dataSource;
    private InitService initService;
    private MetadataConsumerStarter metadataConsumerStarter;

    @Inject
    public Application(Props props, DataSource dataSource, InitService initService,
                       MetadataConsumerStarter metadataConsumerStarter) {
        this.props = props;
        this.dataSource = dataSource;
        this.initService = initService;
        this.metadataConsumerStarter = metadataConsumerStarter;
    }

    public static void main(String[] args) {
        logger.info("Starting Jetty Kun Web Server...");

        /* Initialize Guice Injector */
        Props props = PropsUtils.loadAppProps();
        final Injector injector = Guice.createInjector(
                new KunWebServerModule(props),
                new CommonModule(props),
                new KunMetadataModule(props)
        );

        injector.getInstance(Application.class).start();
        injector.getInstance(KunWebServer.class).start();
    }

    private void start() {
        initService.initDataBuilder();
        initService.publishRpcServices();
        configureDB();

        // start kafka consumer
        metadataConsumerStarter.start();
    }

    private void configureDB() {
        DatabaseSetup setup = new DatabaseSetup(dataSource, props);
        setup.start();
    }

}
