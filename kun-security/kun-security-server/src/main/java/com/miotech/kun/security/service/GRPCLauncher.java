package com.miotech.kun.security.service;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.security.annotation.GrpcServer;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnExpression("#{environment.getActiveProfiles()[0] != 'test'}")
public class GRPCLauncher implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${grpc.server.port}")
    private int port;

    private Server server;

    /**
     * Start serving requests.
     */
    public void start(Map<String, Object> gRPCServers) {
        try {
            ServerBuilder serverBuilder = ServerBuilder.forPort(port);
            for (Object value : gRPCServers.values()) {
                serverBuilder.addService((BindableService) value);
            }
            server = serverBuilder.build().start();
            log.info("security rpc server started, listening on " + port);
            server.awaitTermination();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                log.debug("*** shutting down gRPC server since JVM is shutting down");
                try {
                    GRPCLauncher.this.stop();
                } catch (InterruptedException e) {
                    // do nothing
                }
                log.debug("*** server shut down");
            }));
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, Object> gRPCServers = event.getApplicationContext().getBeansWithAnnotation(GrpcServer.class);
        new Thread(() -> start(gRPCServers)).start();
    }
}
