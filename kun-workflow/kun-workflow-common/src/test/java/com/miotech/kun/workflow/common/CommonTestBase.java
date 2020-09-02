package com.miotech.kun.workflow.common;

import com.miotech.kun.commons.testing.DatabaseTestBase;
import org.junit.After;
import org.junit.ClassRule;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Neo4jContainer;

import java.util.HashMap;

import static com.miotech.kun.commons.db.DatabaseModule.NEO4J_DOMAIN_CLASSES;

public class CommonTestBase extends DatabaseTestBase {
    private static final Logger logger = LoggerFactory.getLogger(CommonTestBase.class);

    @ClassRule
    public static Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j:3.5.20")
                    .withAdminPassword("Mi0tech2020");

    protected SessionFactory neo4jSessionFactory = initNeo4jSessionFactory();

    @Override
    protected void configuration() {
        bind(SessionFactory.class, neo4jSessionFactory);
        super.configuration();
    }

    public SessionFactory initNeo4jSessionFactory() {
        Configuration config = new Configuration.Builder()
                .uri(neo4jContainer.getBoltUrl())
                .connectionPoolSize(50)
                .credentials("neo4j", "Mi0tech2020")
                .build();
        return new SessionFactory(config, NEO4J_DOMAIN_CLASSES);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        clearGraph();
    }

    private void clearGraph() {
        Session session = neo4jSessionFactory.openSession();
        // delete all nodes with relationships
        session.query("match (n) -[r] -> () delete n, r;", new HashMap<>());
        // delete nodes that have no relationships
        session.query("match (n) delete n;", new HashMap<>());
    }
}
