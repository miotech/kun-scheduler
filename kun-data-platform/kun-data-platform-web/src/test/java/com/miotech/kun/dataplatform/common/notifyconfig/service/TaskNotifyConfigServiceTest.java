package com.miotech.kun.dataplatform.common.notifyconfig.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.notifyconfig.dao.TaskNotifyConfigDao;
import com.miotech.kun.dataplatform.mocking.MockTaskNotifyConfigFactory;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class TaskNotifyConfigServiceTest extends AppTestBase {
    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

    @Autowired
    private TaskNotifyConfigDao taskNotifyConfigDao;

    @Test
    public void fetchTaskNotifyConfigById_whenConfigExists_shouldReturnWrappedObject() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithoutId();
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(config);

        // 2. Process
        Optional<TaskNotifyConfig> fetchedResult = taskNotifyConfigService.fetchTaskNotifyConfigById(persistedConfig.getId());

        // 3. Validate
        assertTrue(fetchedResult.isPresent());
        assertThat(fetchedResult.get(), sameBeanAs(persistedConfig));
    }

    @Test
    public void fetchTaskNotifyConfigById_whenConfigNotExists_shouldReturnEmpty() {
        // 1. Prepare
        // 2. Process
        Optional<TaskNotifyConfig> configOptional = taskNotifyConfigService.fetchTaskNotifyConfigById(1234L);
        // 3. Validate
        assertFalse(configOptional.isPresent());
    }

    @Test
    public void fetchTaskNotifyConfigByWorkflowTaskId_whenConfigExists_shouldReturnWrappedObject() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithoutId();
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(config);

        // 2. Process
        Optional<TaskNotifyConfig> fetchedResult =
                taskNotifyConfigService.fetchTaskNotifyConfigByWorkflowTaskId(persistedConfig.getWorkflowTaskId());

        // 3. Validate
        assertTrue(fetchedResult.isPresent());
        assertThat(fetchedResult.get(), sameBeanAs(persistedConfig));
    }

    @Test
    public void fetchTaskNotifyConfigByWorkflowTaskId_whenConfigNotExists_shouldReturnEmpty() {
        // 1. Prepare
        // 2. Process
        Optional<TaskNotifyConfig> configOptional = taskNotifyConfigService.fetchTaskNotifyConfigByWorkflowTaskId(1234L);
        // 3. Validate
        assertFalse(configOptional.isPresent());
    }

    @Test
    public void upsertTaskNotifyConfig_whenIdNotPresented_shouldDoCreate() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithoutId();
        // 2. Process
        TaskNotifyConfig persistedConfig = taskNotifyConfigService.upsertTaskNotifyConfig(config);
        // 3. Validate
        assertThat(persistedConfig, sameBeanAs(config).ignoring("id"));
    }

    @Test
    public void upsertTaskNotifyConfig_whenIdPresented_shouldDoUpdate() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithoutId();
        TaskNotifyConfig configBeforeUpdate = taskNotifyConfigDao.create(config);
        TaskNotifyConfig configToUpdate = configBeforeUpdate.cloneBuilder()
                .withTriggerType(TaskStatusNotifyTrigger.ON_SUCCESS)
                .withWorkflowTaskId(12345L)
                .build();
        // 2. Process
        TaskNotifyConfig updatedConfig = taskNotifyConfigService.upsertTaskNotifyConfig(configToUpdate);

        // 3. Validate
        TaskNotifyConfig configAfterUpdate = taskNotifyConfigService.fetchTaskNotifyConfigById(configToUpdate.getId()).get();

        assertThat(updatedConfig, sameBeanAs(configToUpdate));
        assertThat(configAfterUpdate, sameBeanAs(configToUpdate));
    }

    @Test
    public void upsertTaskNotifyConfig_whenIdPresentedButConfigNotExisting_shouldThrowIllegalArgumentException() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithId(12345L);

        // 2. Process & Validate
        try {
            taskNotifyConfigService.upsertTaskNotifyConfig(config);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void removeTaskNotifyConfigById() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithoutId();
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(config);

        // 2. Process
        boolean removeExistingResult = taskNotifyConfigService.removeTaskNotifyConfigById(persistedConfig.getId());
        boolean removeNotFoundResult = taskNotifyConfigService.removeTaskNotifyConfigById(1234L);

        // 3. Validate
        assertTrue(removeExistingResult);
        assertFalse(removeNotFoundResult);
    }

    @Test
    public void removeTaskNotifyConfigByWorkflowTaskId() {
        // 1. Prepare
        TaskNotifyConfig config = MockTaskNotifyConfigFactory.mockWithoutId();
        TaskNotifyConfig persistedConfig = taskNotifyConfigDao.create(config);

        // 2. Process
        boolean removeExistingResult = taskNotifyConfigService.removeTaskNotifyConfigByWorkflowTaskId(persistedConfig.getWorkflowTaskId());
        boolean removeNotFoundResult = taskNotifyConfigService.removeTaskNotifyConfigByWorkflowTaskId(1234L);

        // 3. Validate
        assertTrue(removeExistingResult);
        assertFalse(removeNotFoundResult);
    }
}
