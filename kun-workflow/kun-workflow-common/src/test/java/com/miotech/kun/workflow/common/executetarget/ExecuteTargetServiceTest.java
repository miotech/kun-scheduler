package com.miotech.kun.workflow.common.executetarget;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ExecuteTargetServiceTest extends DatabaseTestBase {

    @Inject
    private ExecuteTargetService executeTargetService;

    @Inject
    private DatabaseOperator databaseOperator;

    @Override
    protected void configuration() {
        super.configuration();
    }

    @BeforeEach
    public void init() {
        databaseOperator.update("truncate table kun_wf_target RESTART IDENTITY");
    }

    @Test
    public void fetchTargetByName() {
        //prepare
        ExecuteTarget executeTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .withProperties(ImmutableMap.of("schema", "test"))
                .build();
        executeTargetService.createExecuteTarget(executeTarget);

        ExecuteTarget fetched = executeTargetService.fetchExecuteTarget("test");

        //verify
        assertThat(fetched.getName(), is(executeTarget.getName()));
        assertThat(fetched.getProperties(), aMapWithSize(1));
        assertThat(fetched.getProperties(), hasEntry("schema", "test"));

    }

    @Test
    public void fetchTargetById() {
        //prepare
        ExecuteTarget executeTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .withProperties(ImmutableMap.of("schema", "test"))
                .build();
        executeTargetService.createExecuteTarget(executeTarget);

        ExecuteTarget fetched = executeTargetService.fetchExecuteTarget(1l);

        //verify
        assertThat(fetched.getName(), is(executeTarget.getName()));
        assertThat(fetched.getId(), is(1l));
        assertThat(fetched.getProperties(), aMapWithSize(1));
        assertThat(fetched.getProperties(), hasEntry("schema", "test"));
    }

    @Test
    public void fetchTargets() {
        //prepare
        ExecuteTarget testTarget = new ExecuteTarget(null, "test", new HashMap<>());
        executeTargetService.createExecuteTarget(testTarget);

        ExecuteTarget prodTarget = new ExecuteTarget(null, "prod", new HashMap<>());
        executeTargetService.createExecuteTarget(prodTarget);

        ExecuteTarget devTarget = new ExecuteTarget(null, "dev", new HashMap<>());
        executeTargetService.createExecuteTarget(devTarget);

        List<ExecuteTarget> fetchedTargets = executeTargetService.fetchExecuteTargets();

        //verify
        assertThat(fetchedTargets, hasSize(3));
        assertThat(fetchedTargets.get(0).getName(), is("test"));
        assertThat(fetchedTargets.get(1).getName(), is("prod"));
        assertThat(fetchedTargets.get(2).getName(), is("dev"));
    }

    @Test
    public void updateTargetWithExistName_should_throw_exception() {
        //prepare
        ExecuteTarget testTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(testTarget);
        ExecuteTarget prodTarget = ExecuteTarget.newBuilder()
                .withName("prod")
                .build();
        executeTargetService.createExecuteTarget(prodTarget);
        ExecuteTarget fetched = executeTargetService.fetchExecuteTarget("test");
        ExecuteTarget updateTarget = fetched.cloneBuilder().withName("prod").build();

        // verify
        Exception ex = assertThrows(IllegalArgumentException.class, () -> executeTargetService.updateExecuteTarget(updateTarget));
        assertEquals("target name = " + updateTarget.getName() + "is exist", ex.getMessage());
    }

    @Test
    public void createTargetWithExistName_should_throw_exception() {
        //prepare
        ExecuteTarget testTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(testTarget);
        ExecuteTarget newTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();

        // verify
        Exception ex = assertThrows(IllegalArgumentException.class, () -> executeTargetService.createExecuteTarget(newTarget));
        assertEquals("target name = " + newTarget.getName() + "is exist", ex.getMessage());
    }
}
