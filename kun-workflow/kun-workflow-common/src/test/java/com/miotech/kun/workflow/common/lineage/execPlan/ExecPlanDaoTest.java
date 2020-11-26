package com.miotech.kun.workflow.common.lineage.execPlan;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.core.model.lineage.ExecPlan;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class ExecPlanDaoTest extends DatabaseTestBase {

    @Inject
    private ExecPlanDao execPlanDao;

    @Test
    public void testGetExecPlanWithNullSource(){
        ExecPlan execPlan = ExecPlan.newBuilder()
                .withTaskName("test exec plan")
                .withInputSources(null)
                .withOutputSource(null)
                .build();
        execPlanDao.saveExecPlan(execPlan);

        List<ExecPlan> execPlanList = execPlanDao.getExecPlanByTaskName("test exec plan");
        assertThat(execPlanList,containsInAnyOrder(execPlan));
    }
}
