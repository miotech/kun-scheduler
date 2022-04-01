package com.miotech.kun.monitor.alert.service;

import com.miotech.kun.monitor.alert.MonitorAlertTestBase;
import com.miotech.kun.monitor.alert.mocking.MockWeComBaseResultFactory;
import com.miotech.kun.monitor.alert.mocking.MockWeComGetTokenResultFactory;
import com.miotech.kun.monitor.alert.model.WeComBaseResult;
import com.miotech.kun.monitor.alert.model.WeComGetTokenResult;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;

public class WeComSenderTest extends MonitorAlertTestBase {

    @Value("${notify.wecom.url.get_token}")
    private String getTokenUrl;

    @Value("${notify.wecom.corpid}")
    private String corpid;

    @Value("${notify.wecom.corpsecret}")
    private String corpsecret;

    @Value("${notify.wecom.url.send-to-user}")
    private String sendToUserUrl;

    @Value("${notify.wecom.url.send-to-chat}")
    private String sendToChatUrl;

    @Autowired
    private WeComSender weComSender;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testSendMessage_toUser_success() {
        // mock get token
        String getTokenUrl = String.format(this.getTokenUrl, corpid, corpsecret);
        WeComGetTokenResult weComGetTokenResult = MockWeComGetTokenResultFactory.create();
        ResponseEntity<WeComGetTokenResult> weComGetTokenResultResponseEntity = new ResponseEntity(weComGetTokenResult, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(getTokenUrl), Mockito.eq(HttpMethod.GET), Mockito.any(),
                Mockito.any(Class.class))).willReturn(weComGetTokenResultResponseEntity);

        // mock send msg to user
        String url = String.format(sendToUserUrl, weComGetTokenResult.getAccessToken());
        WeComBaseResult weComBaseResult = MockWeComBaseResultFactory.create();
        ResponseEntity<WeComBaseResult> weComBaseResultResponseEntity = new ResponseEntity(weComBaseResult, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(url), Mockito.eq(HttpMethod.POST), Mockito.any(),
                Mockito.any(Class.class))).willReturn(weComBaseResultResponseEntity);

        List<String> weComIds = ImmutableList.of("user1", "user2");
        String msg = "test msg";
        boolean result = weComSender.sendMessageToUsers(weComIds, msg);

        Assert.assertTrue(result);
    }

    @Test
    public void testSendMessage_toUser_weComIdsIsEmpty() {
        List<String> weComIds = ImmutableList.of();
        String msg = "test msg";
        boolean result = weComSender.sendMessageToUsers(weComIds, msg);

        Assert.assertFalse(result);
    }

    @Test
    public void testSendMessage_toUser_failed() {
        // mock get token
        String getTokenUrl = String.format(this.getTokenUrl, corpid, corpsecret);
        WeComGetTokenResult weComGetTokenResult = MockWeComGetTokenResultFactory.create();
        ResponseEntity<WeComGetTokenResult> weComGetTokenResultResponseEntity = new ResponseEntity(weComGetTokenResult, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(getTokenUrl), Mockito.eq(HttpMethod.GET), Mockito.any(),
                Mockito.any(Class.class))).willReturn(weComGetTokenResultResponseEntity);

        // mock send msg to user
        String url = String.format(sendToUserUrl, weComGetTokenResult.getAccessToken());
        WeComBaseResult weComBaseResult = MockWeComBaseResultFactory.create(1, "invalid token");
        ResponseEntity<WeComBaseResult> weComBaseResultResponseEntity = new ResponseEntity(weComBaseResult, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(url), Mockito.eq(HttpMethod.POST), Mockito.any(),
                Mockito.any(Class.class))).willReturn(weComBaseResultResponseEntity);

        List<String> weComIds = ImmutableList.of("user1", "user2");
        String msg = "test msg";
        boolean result = weComSender.sendMessageToUsers(weComIds, msg);

        Assert.assertFalse(result);
    }

    @Test
    public void testSendMessage_toChat_success() {
        // mock get token
        String getTokenUrl = String.format(this.getTokenUrl, corpid, corpsecret);
        WeComGetTokenResult weComGetTokenResult = MockWeComGetTokenResultFactory.create();
        ResponseEntity<WeComGetTokenResult> weComGetTokenResultResponseEntity = new ResponseEntity(weComGetTokenResult, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(getTokenUrl), Mockito.eq(HttpMethod.GET), Mockito.any(),
                Mockito.any(Class.class))).willReturn(weComGetTokenResultResponseEntity);

        // mock send msg to user
        String url = String.format(sendToChatUrl, weComGetTokenResult.getAccessToken());
        WeComBaseResult weComBaseResult = MockWeComBaseResultFactory.create();
        ResponseEntity<WeComBaseResult> weComBaseResultResponseEntity = new ResponseEntity(weComBaseResult, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(url), Mockito.eq(HttpMethod.POST), Mockito.any(),
                Mockito.any(Class.class))).willReturn(weComBaseResultResponseEntity);

        String chatid = "1";
        String msg = "test msg";
        boolean result = weComSender.sendMessageToChat(chatid, msg);

        Assert.assertTrue(result);
    }

    @Test
    public void testSendMessage_toChat_weComIdsIsEmpty() {
        String chatid = null;
        String msg = "test msg";
        boolean result = weComSender.sendMessageToChat(chatid, msg);

        Assert.assertFalse(result);
    }

    @Test
    public void testSendMessage_toChat_failed() {
        // mock get token
        String getTokenUrl = String.format(this.getTokenUrl, corpid, corpsecret);
        WeComGetTokenResult weComGetTokenResult = MockWeComGetTokenResultFactory.create();
        ResponseEntity<WeComGetTokenResult> weComGetTokenResultResponseEntity = new ResponseEntity(weComGetTokenResult, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(getTokenUrl), Mockito.eq(HttpMethod.GET), Mockito.any(),
                Mockito.any(Class.class))).willReturn(weComGetTokenResultResponseEntity);

        // mock send msg to user
        String url = String.format(sendToChatUrl, weComGetTokenResult.getAccessToken());
        WeComBaseResult weComBaseResult = MockWeComBaseResultFactory.create(1, "invalid token");
        ResponseEntity<WeComBaseResult> weComBaseResultResponseEntity = new ResponseEntity(weComBaseResult, HttpStatus.OK);
        BDDMockito.given(restTemplate.exchange(Mockito.eq(url), Mockito.eq(HttpMethod.POST), Mockito.any(),
                Mockito.any(Class.class))).willReturn(weComBaseResultResponseEntity);

        String chatid = "1";
        String msg = "test msg";
        boolean result = weComSender.sendMessageToChat(chatid, msg);

        Assert.assertFalse(result);
    }


}
