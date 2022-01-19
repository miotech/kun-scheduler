package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class SchedulerTestBase extends DatabaseTestBase {
    @Container
    public static Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j:3.5.20")
            .withAdminPassword("Mi0tech2020");



    @Override
    protected void configuration() {
        super.configuration();
        bind(EventBus.class, new EventBus());
        addModules(new GraphDatabaseModule(
                neo4jContainer.getBoltUrl(),
                "neo4j",
                neo4jContainer.getAdminPassword())
        );
    }
}
