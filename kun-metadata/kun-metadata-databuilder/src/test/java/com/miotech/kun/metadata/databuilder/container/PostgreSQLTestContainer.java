package com.miotech.kun.metadata.databuilder.container;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLTestContainer {

    private static final String DEFAULT_IMAGE_VERSION = "postgres:12.3";

    private PostgreSQLTestContainer() {
    }

    public static PostgreSQLContainer start(String imageVersion) {
        PostgreSQLContainer container = new PostgreSQLContainer(imageVersion);
        container.start();
        return container;
    }

    public static PostgreSQLContainer start() {
        return start(DEFAULT_IMAGE_VERSION);
    }

    public static PostgreSQLContainer executeInitSQLThenStart(String initScriptPath) {
        PostgreSQLContainer container = new PostgreSQLContainer(DEFAULT_IMAGE_VERSION);
        container.withInitScript(initScriptPath).start();
        return container;
    }


}
