package com.miotech.kun.commons.db;

import com.google.inject.AbstractModule;
import com.miotech.kun.commons.utils.Props;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphDatabaseModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseModule.class);

    private final Props props;

    private final String[] domainClasses;

    public static final String[] DEFAULT_NEO4J_DOMAIN_CLASSES = {
            "com.miotech.kun.workflow.common.lineage.node"
    };

    public GraphDatabaseModule(Props props) {
        this.props = props;
        this.domainClasses = DEFAULT_NEO4J_DOMAIN_CLASSES;
    }

    public GraphDatabaseModule(Props props, String[] domainClasses) {
        this.props = props;
        this.domainClasses = domainClasses;
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
