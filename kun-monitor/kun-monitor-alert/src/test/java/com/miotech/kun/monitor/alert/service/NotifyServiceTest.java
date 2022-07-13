package com.miotech.kun.monitor.alert.service;

import com.miotech.kun.monitor.alert.MonitorAlertTestBase;
import com.miotech.kun.monitor.alert.common.service.TaskNotifyConfigService;
import com.miotech.kun.monitor.alert.mocking.MockUserInfoFactory;
import com.miotech.kun.monitor.facade.model.alert.TaskNotifyConfig;
import com.miotech.kun.monitor.facade.model.alert.TaskStatusNotifyTrigger;
import com.miotech.kun.security.model.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;

import static org.mockito.Mockito.*;

public class NotifyServiceTest extends MonitorAlertTestBase {

    @MockBean
    private EmailService emailService;

    @MockBean
    private WeComService weComService;

    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

    @Autowired
    private NotifyService notifyService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testNotify_useSystemDefaultConfig() {
        // 1. Prepare
        long attemptId = 1L;
        long taskId = 1230L;

        // Register with system default trigger
        taskNotifyConfigService.upsertTaskNotifyConfig(TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(taskId)
                // Should goes as system default config
                .withTriggerType(TaskStatusNotifyTrigger.SYSTEM_DEFAULT)
                // no notifier config required
                .withNotifierConfigs(Lists.newArrayList())
                .build());

        notifyService.notify(taskId, StringUtils.EMPTY, StringUtils.EMPTY);

        verify(weComService, times(1)).sendMessage(anyLong(), anyString());

    }

    @Test
    public void testNotify_usernames_empty() {
        List<String> usernames = Lists.emptyList();
        String msg = "test msg";
        notifyService.notify(usernames, msg);

        verify(weComService, never()).sendMessage(anyList(), anyString());
    }

}
