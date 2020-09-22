package com.miotech.kun.commons.db;

import com.google.inject.AbstractModule;
import com.miotech.kun.commons.utils.Props;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Neo4jContainer;

public class GraphDatabaseModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseModule.class);

    private final Props props;

    private final String[] domainClasses;

    private final boolean useTestContainer;

    private final Neo4jContainer neo4jContainer;

    public static final String[] DEFAULT_NEO4J_DOMAIN_CLASSES = {
            "com.miotech.kun.workflow.common.lineage.node"
    };

    public GraphDatabaseModule(Props props) {
        this.props = props;
        this.domainClasses = DEFAULT_NEO4J_DOMAIN_CLASSES;
        this.neo4jContainer = null;
        this.useTestContainer = false;
    }

    public GraphDatabaseModule(Props props, String[] domainClasses) {
        this.props = props;
        this.domainClasses = domainClasses;
        this.neo4jContainer = null;
        this.useTestContainer = false;
    }

    public GraphDatabaseModule(Neo4jContainer container) {
        this.props = new Props();
        this.domainClasses = DEFAULT_NEO4J_DOMAIN_CLASSES;
        this.neo4jContainer = container;
        this.useTestContainer = true;

        this.props.put("neo4j.uri", container.getBoltUrl());
        this.props.put("neo4j.username", "neo4j");
        this.props.put("neo4j.password", container.getAdminPassword());
    }

    public GraphDatabaseModule(Neo4jContainer container, String[] domainClasses) {
        this.props = new Props();
        this.domainClasses = domainClasses;
        this.neo4jContainer = container;
        this.useTestContainer = true;

        this.props.put("neo4j.uri", container.getBoltUrl());
        this.props.put("neo4j.username", "neo4j");
        this.props.put("neo4j.password", container.getAdminPassword());
    }

    public Neo4jContainer getNeo4jContainer() {
        return this.neo4jContainer;
    }

    public boolean isUsingContainer() {
        return this.useTestContainer;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(SessionFactory.class)
                .toInstance(provideNeo4jSessionFactory(this.props));
    }

    public SessionFactory provideNeo4jSessionFactory(Props props) {
        if (!props.containsKey("neo4j.uri")) {
            logger.info("neo4j configuration not found. Skip creating SessionFactory...");
            return null;
        }
        Configuration config = new Configuration.Builder()
                .uri(props.getString("neo4j.uri"))
                .connectionPoolSize(50)
                .credentials(props.getString("neo4j.username"), props.getString("neo4j.password"))
                .build();
        return new SessionFactory(config, this.domainClasses);
    }
}
