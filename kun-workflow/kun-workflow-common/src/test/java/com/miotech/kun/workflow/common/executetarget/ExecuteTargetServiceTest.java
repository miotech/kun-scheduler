package com.miotech.kun.workflow.common.executetarget;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ExecuteTargetServiceTest extends DatabaseTestBase {

    @Inject
    private ExecuteTargetService executeTargetService;

    @Inject
    private DatabaseOperator databaseOperator;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Override
    protected void configuration() {
        super.configuration();
        bind(TargetProvider.class,DefaultTargetProvider.class);
    }

    @Before
    public void init(){
        databaseOperator.update("truncate table kun_wf_target RESTART IDENTITY");
    }

    @Test
    public void fetchTargetByName(){
        //prepare
        ExecuteTarget executeTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(executeTarget);

        ExecuteTarget fetched = executeTargetService.fetchExecuteTarget("test");

        //verify
        assertThat(fetched.getName(),is(executeTarget.getName()));
    }

    @Test
    public void fetchTargetById(){
        //prepare
        ExecuteTarget executeTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(executeTarget);

        ExecuteTarget fetched = executeTargetService.fetchExecuteTarget(1l);

        //verify
        assertThat(fetched.getName(),is(executeTarget.getName()));
        assertThat(fetched.getId(),is(1l));
    }

    @Test
    public void fetchTargets(){
        //prepare
        ExecuteTarget testTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(testTarget);

        ExecuteTarget prodTarget = ExecuteTarget.newBuilder()
                .withName("prod")
                .build();
        executeTargetService.createExecuteTarget(prodTarget);

        ExecuteTarget devTarget = ExecuteTarget.newBuilder()
                .withName("dev")
                .build();
        executeTargetService.createExecuteTarget(devTarget);

        List<ExecuteTarget> fetchedTargets = executeTargetService.fetchExecuteTargets();

        //verify
        assertThat(fetchedTargets,hasSize(3));
        assertThat(fetchedTargets.get(0).getName(),is("test"));
        assertThat(fetchedTargets.get(1).getName(),is("prod"));
        assertThat(fetchedTargets.get(2).getName(),is("dev"));
    }

    @Test
    public void updateTargetWithExistName_should_throw_exception(){
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
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("target name = " + updateTarget.getName() + "is exist");

        executeTargetService.updateExecuteTarget(updateTarget);
    }

    @Test
    public void createTargetWithExistName_should_throw_exception(){
        //prepare
        ExecuteTarget testTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();
        executeTargetService.createExecuteTarget(testTarget);
        ExecuteTarget newTarget = ExecuteTarget.newBuilder()
                .withName("test")
                .build();

        // verify
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("target name = " + newTarget.getName() + "is exist");
        executeTargetService.createExecuteTarget(newTarget);
    }
}
