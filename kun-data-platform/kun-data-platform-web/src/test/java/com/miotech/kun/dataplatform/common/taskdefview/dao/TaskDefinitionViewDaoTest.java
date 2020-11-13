package com.miotech.kun.dataplatform.common.taskdefview.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionCreateInfoVO;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionViewFactory;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.*;

import static com.miotech.kun.dataplatform.common.taskdefview.dao.TaskDefinitionViewDao.TASK_DEF_VIEW_TABLE_NAME;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TaskDefinitionViewDaoTest extends AppTestBase {
    private final Logger logger = LoggerFactory.getLogger(TaskDefinitionViewDaoTest.class);

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskDefinitionViewDao taskDefinitionViewDao;

    private long prepareTaskDefinitionViewInstanceAndInsert() {
        TaskDefinitionView view = MockTaskDefinitionViewFactory.createTaskDefView();
        logger.debug("Preparing mock view with id = {}", view.getId());
        insertMockViewIntoDatabase(view);
        return view.getId();
    }

    private int insertMockViewIntoDatabase(TaskDefinitionView view) {
        //noinspection SqlResolve
        String sql = "INSERT INTO " + TASK_DEF_VIEW_TABLE_NAME
                + " (id, name, creator, last_modifier, create_time, update_time) VALUES "
                + " (?, ?, ?, ?, ?, ?);";
        return jdbcTemplate.update(
                sql,
                view.getId(),
                view.getName(),
                view.getCreator(),
                view.getLastModifier(),
                view.getCreateTime(),
                view.getUpdateTime()
        );
    }

    @Test
    public void fetchById_ViewExists_shouldReturnWrappedObject() {
        // Prepare
        long insertedViewId = prepareTaskDefinitionViewInstanceAndInsert();
        // Process
        Optional<TaskDefinitionView> viewOptional = taskDefinitionViewDao.fetchById(insertedViewId);
        // Validate
        assertTrue(viewOptional.isPresent());
        assertEquals(Long.valueOf(insertedViewId), viewOptional.get().getId());
    }

    @Test
    public void fetchById_ViewNotExists_shouldReturnEmptyOptionalObject() {
        // Process
        Optional<TaskDefinitionView> viewOptional = taskDefinitionViewDao.fetchById(1234L);
        assertFalse(viewOptional.isPresent());
    }

    @Test
    public void fetchBySearchParams_withPaginationConfig_shouldReturnPageList() {
        // Prepare: insert 50 task definition instances
        List<Long> insertedViewIds = new ArrayList<>();
        for (int i = 0; i < 50; ++i) {
            insertedViewIds.add(prepareTaskDefinitionViewInstanceAndInsert());
        }
        // Process
        TaskDefinitionViewSearchParams searchParams1 = new TaskDefinitionViewSearchParams(
                "", 1, 25
        );
        List<TaskDefinitionView> viewPage1 = taskDefinitionViewDao.fetchListBySearchParams(searchParams1);

        TaskDefinitionViewSearchParams searchParams2 = new TaskDefinitionViewSearchParams(
                "", 2, 25
        );
        List<TaskDefinitionView> viewPage2 = taskDefinitionViewDao.fetchListBySearchParams(searchParams2);

        TaskDefinitionViewSearchParams searchParams3 = new TaskDefinitionViewSearchParams(
                "", 3, 25
        );
        List<TaskDefinitionView> viewPage3 = taskDefinitionViewDao.fetchListBySearchParams(searchParams3);

        TaskDefinitionViewSearchParams searchParams4 = new TaskDefinitionViewSearchParams(
                "", 3, 20
        );
        List<TaskDefinitionView> viewPage4 = taskDefinitionViewDao.fetchListBySearchParams(searchParams4);

        // Validate
        assertThat(viewPage1.size(), is(25));
        assertThat(viewPage2.size(), is(25));
        assertThat(viewPage3.size(), is(0));
        assertThat(viewPage4.size(), is(10));
    }

    @Test
    public void create_withValidConfig_shouldPersistModel() {
        // Freeze create time
        OffsetDateTime createTimeExpected = DateTimeUtils.freeze();
        TaskDefinitionCreateInfoVO createInfoVO = new TaskDefinitionCreateInfoVO("test_view", 1L, new ArrayList<>());

        // Prepare
        TaskDefinitionView createdView = taskDefinitionViewDao.create(createInfoVO);

        // Validate
        assertThat(createdView.getName(), is(createdView.getName()));
        assertThat(createdView.getCreator(), is(createdView.getCreator()));
        assertThat(createdView.getCreateTime(), is(createTimeExpected));
        assertThat(createdView.getUpdateTime(), is(createTimeExpected));

        // Teardown
        DateTimeUtils.resetClock();
    }

    @Test
    public void create_withExistingTaskDefinitions_shouldWork() {
        // Prepare
        List<Long> inclusiveTaskDefinitionIds = new ArrayList<>();
        List<TaskDefinition> mockTaskDefinitions = MockTaskDefinitionFactory.createTaskDefinitions(5);
        for (TaskDefinition taskDef : mockTaskDefinitions) {
            taskDefinitionDao.create(taskDef);
            inclusiveTaskDefinitionIds.add(taskDef.getDefinitionId());
        }
        TaskDefinitionCreateInfoVO createInfoVO = new TaskDefinitionCreateInfoVO(
                "test_view", 1L, inclusiveTaskDefinitionIds
        );

        // Prepare
        TaskDefinitionView createdView = taskDefinitionViewDao.create(createInfoVO);

        Set<TaskDefinition> taskDefinitionSet = new HashSet<>(mockTaskDefinitions);
        Set<TaskDefinition> fetchedTaskDefinitionSet = new HashSet<>(createdView.getIncludedTaskDefinitions());

        // Validate
        assertThat(createdView.getIncludedTaskDefinitions().size(), is(inclusiveTaskDefinitionIds.size()));
        assertThat(fetchedTaskDefinitionSet, sameBeanAs(taskDefinitionSet));
    }
}
