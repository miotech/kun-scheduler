package com.miotech.kun.monitor.alert.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.monitor.alert.model.WeComBaseResult;
import com.miotech.kun.monitor.alert.model.WeComGetTokenResult;
import com.miotech.kun.monitor.alert.model.WeComToGroupMessage;
import com.miotech.kun.monitor.alert.model.WeComToUserMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WeComSender {

    private final static Cache<String, String> weConTokenCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();
    private final static String WECOM_TOKEN_KEY = "we_com_token";

    @Value("${notify.wecom.url.get_token}")
    private String getTokenUrl;

    @Value("${notify.wecom.url.send-to-user}")
    private String sendToUserUrl;

    @Value("${notify.wecom.url.send-to-group}")
    private String sendToGroupUrl;

    @Value("${notify.wecom.agentid}")
    private int agentid;

    @Value("${notify.wecom.corpid}")
    private String corpid;

    @Value("${notify.wecom.corpsecret}")
    private String corpsecret;

    @Autowired
    private RestTemplate restTemplate;

    public void sendMessage(List<String> weComUserIds, String msg) {
        if (CollectionUtils.isEmpty(weComUserIds)) {
            return;
        }

        String token = getToken();
        String url = String.format(sendToUserUrl, token);
        WeComToUserMessage weComToUserMessage = WeComToUserMessage.from(StringUtils.join(weComUserIds, "|"), agentid, msg);
        log.debug("ready to send message: {} to wecom user: {}", msg, weComToUserMessage.getTouser());
        WeComBaseResult weComBaseResult = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(weComToUserMessage), WeComBaseResult.class).getBody();
        if (!weComBaseResult.isSuccess()) {
            log.error("send message: {} to wecom user: {} failed.", msg, weComToUserMessage.getTouser());
        }
    }

    public void sendMessage(String chatid, String msg) {
        if (StringUtils.isBlank(chatid)) {
            return;
        }

        String token = getToken();
        String url = String.format(sendToGroupUrl, token);
        WeComToGroupMessage weComToGroupMessage = WeComToGroupMessage.from(chatid, msg);
        log.debug("ready to send message: {} to wecom group: {}", msg, chatid);
        WeComBaseResult weComBaseResult = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(weComToGroupMessage), WeComBaseResult.class).getBody();
        if (!weComBaseResult.isSuccess()) {
            log.error("send message: {} to wecom group: {} failed.", msg, chatid);
        }
    }


    private String getToken() {
        try {
            return weConTokenCache.get(WECOM_TOKEN_KEY, () -> {
                String getTokenUrl = String.format(this.getTokenUrl, corpid, corpsecret);
                WeComGetTokenResult weComGetTokenResult = restTemplate.exchange(getTokenUrl, HttpMethod.GET, null, WeComGetTokenResult.class).getBody();
                return weComGetTokenResult.getAccessToken();
            });
        } catch (ExecutionException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }


}
