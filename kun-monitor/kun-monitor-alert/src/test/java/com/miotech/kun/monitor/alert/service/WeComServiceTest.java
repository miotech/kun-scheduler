package com.miotech.kun.monitor.alert.service;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.monitor.alert.MonitorAlertTestBase;
import com.miotech.kun.monitor.alert.mocking.MockTaskAttemptStatusChangeEventFactory;
import com.miotech.kun.monitor.alert.mocking.MockUserInfoFactory;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;

import static org.mockito.Mockito.*;

public class WeComServiceTest extends MonitorAlertTestBase {

    @Autowired
    private WeComService weComService;

    @SpyBean
    private DeployedTaskFacade deployedTaskFacade;

    @MockBean
    private WeComSender weComSender;

    @Test
    public void sendMessage_event() {
        TaskAttemptStatusChangeEvent event = MockTaskAttemptStatusChangeEventFactory.create(true);
        UserInfo userInfo = MockUserInfoFactory.create();
        doReturn(userInfo).when(deployedTaskFacade).getUserByTaskId(event.getTaskId());

        weComService.sendMessage(event);
        verify(weComSender, times(1)).sendMessageToUsers(anyList(), anyString());
        verify(weComSender, times(1)).sendMessageToChat(anyString(), anyString());
    }

    @Test
    public void sendMessage_taskId() {
        long taskId = IdGenerator.getInstance().nextId();
        String msg = "test msg";
        UserInfo userInfo = MockUserInfoFactory.create();
        doReturn(userInfo).when(deployedTaskFacade).getUserByTaskId(taskId);

        weComService.sendMessage(taskId, msg);
        verify(weComSender, times(1)).sendMessageToUsers(anyList(), anyString());
        verify(weComSender, times(1)).sendMessageToChat(anyString(), anyString());
    }

    @Test
    public void sendMessage_user() {
        List<String> weComUserIds = ImmutableList.of("user1", "user2");
        String msg = "test msg";

        weComService.sendMessage(weComUserIds, msg);
        verify(weComSender, times(1)).sendMessageToUsers(anyList(), anyString());
    }
}
