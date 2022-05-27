package com.miotech.kun.workflow.common.rpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ExecutorServer {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServer.class);

    private final int port;
    private final Server server;

    public ExecutorServer(int port) throws IOException {
        this(ServerBuilder.forPort(port), port);
    }

    /** Create a RouteGuide server using serverBuilder as a base and features as data. */
    public ExecutorServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        server = serverBuilder.build();
    }

    /** Start serving requests. */
    public void start() throws IOException {
        logger.info("executor rpc server started, listening on " + port);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ExecutorServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
