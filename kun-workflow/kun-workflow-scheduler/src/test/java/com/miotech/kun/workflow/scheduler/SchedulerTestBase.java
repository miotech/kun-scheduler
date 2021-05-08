package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.workflow.testing.WorkflowDatabaseTestBase;
import org.junit.ClassRule;
import org.testcontainers.containers.Neo4jContainer;

public class SchedulerTestBase extends WorkflowDatabaseTestBase {
    @ClassRule
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
