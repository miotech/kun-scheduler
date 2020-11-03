package com.miotech.kun.security.service;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.common.ConfigKey;
import com.miotech.kun.security.model.bo.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
public class BaseSecurityService {

    @Value("${security.pass-token:40A4C5379B73F31D6CD24F6A7C5C3ACB}")
    String passToken;

    /**
     * separate from app rest template
     */
    RestTemplate restTemplate = new RestTemplate();

    public String getCurrentUsername() {
        UserInfo userInfo = getCurrentUser();
        if (userInfo != null) {
            return userInfo.getUsername();
        }
        return "anonymousUser";
    }

    public UserInfo getCurrentUser() {
        return SecurityContextHolder.getUserInfo();
    }

    public UserInfo getUserById(Long id) {
        HttpEntity httpEntity = new HttpEntity(getFinalRequestHttpHeaders());
        String userInfoUrl = ConfigKey.getSecurityServerUserInfoUrl() + id;
        ResponseEntity<RequestResult<UserInfo>> userResult = restTemplate.exchange(userInfoUrl,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<RequestResult<UserInfo>>() {
                });
        if (!userResult.getStatusCode().is2xxSuccessful()) {
            return null;
        }
        return userResult.getBody().getResult();
    }

    private HttpHeaders getFinalRequestHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(ConfigKey.HTTP_REQUEST_PASS_TOKEN_KEY, passToken);
        return httpHeaders;
    }
}
