package com.miotech.kun.commons.db;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphDatabaseModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseModule.class);

    private final String uri;

    private final String username;

    private final String password;

    private final String[] domainClasses;

    public static final String[] DEFAULT_NEO4J_DOMAIN_CLASSES = {
            "com.miotech.kun.workflow.common.lineage.node"
    };

    public GraphDatabaseModule(Props props) {
        this(props, DEFAULT_NEO4J_DOMAIN_CLASSES);
    }

    public GraphDatabaseModule(Props props, String[] domainClasses) {
        this(props.getString("neo4j.uri"),
                props.getString("neo4j.username"),
                props.getString("neo4j.password"),
                domainClasses);
    }

    public GraphDatabaseModule(String uri, String username, String password) {
        this(uri, username, password, DEFAULT_NEO4J_DOMAIN_CLASSES);
    }

    public GraphDatabaseModule(String uri, String username, String password, String[] domainClasses) {
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.domainClasses = domainClasses;
    }

    @Singleton
    @Provides
    private SessionFactory neo4jSessionFactory() {
        Configuration config = new Configuration.Builder()
                .uri(uri)
                .connectionPoolSize(20)
                .credentials(username, password)
                .build();
        return new SessionFactory(config, this.domainClasses);
    }
}
