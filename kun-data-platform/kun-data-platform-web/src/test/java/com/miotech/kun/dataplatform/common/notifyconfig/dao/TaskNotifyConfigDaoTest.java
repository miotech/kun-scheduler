package com.miotech.kun.dataplatform.common.notifyconfig.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.mocking.MockTaskNotifyConfigFactory;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class TaskNotifyConfigDaoTest extends AppTestBase {
    @Autowired
    private TaskNotifyConfigDao taskNotifyConfigDao;

    @Test
    public void create_withProperConfiguration_shouldWork() {
        // 1. Prepare
        TaskNotifyConfig notifyConfig = MockTaskNotifyConfigFactory.mockWithoutId();

        // 2. Process
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(notifyConfig);

        // 3. Validate
        assertThat(persistedConfig, sameBeanAs(notifyConfig).ignoring("id"));
    }

    @Test
    public void create_withImproperConfiguration_shouldThrowException() {
        // 1. Prepare
        TaskNotifyConfig incompleteConfig1 = TaskNotifyConfig.newBuilder()
                .withTriggerType(TaskStatusNotifyTrigger.SYSTEM_DEFAULT)
                .withNotifierConfigs(new ArrayList<>())
                .build();
        TaskNotifyConfig incompleteConfig2 = TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(1234L)
                .withNotifierConfigs(new ArrayList<>())
                .build();

        // 2. Process & Validate
        try {
            taskNotifyConfigDao.create(incompleteConfig1);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }

        try {
            taskNotifyConfigDao.create(incompleteConfig2);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void fetchById_whenConfigExists_shouldReturnWrappedObject() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithoutId();
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(config);

        // 2. Process
        Optional<TaskNotifyConfig> wrappedResult = taskNotifyConfigDao.fetchById(persistedConfig.getId());

        // 3. Validate
        assertTrue(wrappedResult.isPresent());
        assertThat(wrappedResult.get(), sameBeanAs(persistedConfig));
    }

    @Test
    public void fetchByWorkflowTaskId_whenConfigNotExisting_shouldReturnEmpty() {
        // 1. Prepare
        // 2. Process
        Optional<TaskNotifyConfig> wrappedResult = taskNotifyConfigDao.fetchById(1234L);

        // 3. Validate
        assertFalse(wrappedResult.isPresent());
    }

    @Test
    public void update_withConfigItemExisting_shouldWork() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithoutId();
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(config);
        TaskNotifyConfig updateConfig = persistedConfig.cloneBuilder()
                .withTriggerType(TaskStatusNotifyTrigger.NEVER)
                .build();

        // 2. Process
        TaskNotifyConfig updateResult = taskNotifyConfigDao.update(updateConfig);
        TaskNotifyConfig persistedConfigAfterUpdate = taskNotifyConfigDao.fetchById(updateConfig.getId()).get();

        // 3. Validate
        assertThat(updateResult, sameBeanAs(updateConfig));
        assertThat(updateResult, sameBeanAs(persistedConfigAfterUpdate));
    }

    @Test
    public void update_whenConfigItemNotExists_shouldThrowIllegalArgumentException() {
        // 1. Prepare
        TaskNotifyConfig configNotExisting = MockTaskNotifyConfigFactory.mockWithId(1234L);

        // 2. Process & Validate
        try {
            taskNotifyConfigDao.update(configNotExisting);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void removeById_whenConfigItemExists_shouldRemoveAndReturnTrue() {
        // 1. Prepare
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(MockTaskNotifyConfigFactory.mockWithoutId());

        // 2. Process
        boolean removeResult = taskNotifyConfigDao.removeById(persistedConfig.getId());

        // 3. Validate
        assertTrue(removeResult);
        assertFalse(taskNotifyConfigDao.fetchById(persistedConfig.getId()).isPresent());
    }

    @Test
    public void removeById_whenConfigItemNotExists_shouldReturnFalse() {
        // 1. Prepare
        // 2. Process
        boolean removeResult = taskNotifyConfigDao.removeById(1234L);

        // 3. Validate
        assertFalse(removeResult);
    }

    @Test
    public void removeByWorkflowTaskId_whenConfigItemExists_shouldRemoveAndReturnTrue() {
        // 1. Prepare
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(MockTaskNotifyConfigFactory.mockWithoutId());

        // 2. Process
        boolean removeResult = taskNotifyConfigDao.removeByWorkflowTaskId(persistedConfig.getWorkflowTaskId());

        // 3. Validate
        assertTrue(removeResult);
        assertFalse(taskNotifyConfigDao.fetchById(persistedConfig.getId()).isPresent());
    }

    @Test
    public void removeByWorkflowTaskId_whenConfigItemNotExists_shouldReturnFalse() {
        // 1. Prepare
        // 2. Process
        boolean removeResult = taskNotifyConfigDao.removeByWorkflowTaskId(1234L);

        // 3. Validate
        assertFalse(removeResult);
    }
}
