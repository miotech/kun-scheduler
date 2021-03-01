package com.miotech.kun.security.authenticate.filter;

import com.google.common.collect.Lists;
import com.miotech.kun.security.authenticate.resolver.AttributesResolver;
import com.miotech.kun.security.authenticate.resolver.impl.OktaAttributesResolver;
import com.miotech.kun.security.model.UserInfo;
import org.json.simple.JSONObject;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2021/3/3
 */
public class OAuth2AuthorizationCodeFilter extends AbstractAuthenticationProcessingFilter {

    OAuth2ClientProperties clientProperties;

    Map<String, AttributesResolver> attributesResolverMap;

    public OAuth2AuthorizationCodeFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        attributesResolverMap = new HashMap<>();
        attributesResolverMap.put("okta", new OktaAttributesResolver());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String registrationId = request.getParameter("registrationId");
        String clientId = clientProperties.getRegistration().get(registrationId).getClientId();
        String clientSecret = clientProperties.getRegistration().get(registrationId).getClientSecret();
        String redirectUri = clientProperties.getRegistration().get(registrationId).getRedirectUri();
        String tokenUri = clientProperties.getProvider().get(registrationId).getTokenUri();
        String userInfoUri = clientProperties.getProvider().get(registrationId).getUserInfoUri();

        String authorizationCode = request.getParameter("code");

        JSONObject tokenInfos = getAccessTokenInfo(authorizationCode, clientId, clientSecret, redirectUri, tokenUri);
        String accessToken = (String) tokenInfos.get("access_token");

        JSONObject userInfos = getUserInfo(userInfoUri, accessToken);

        String username = attributesResolverMap.get(registrationId).resolveUsername(userInfos);
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);

        OAuth2User oAuth2User = new DefaultOAuth2User(Lists.newArrayList(new SimpleGrantedAuthority("OAUTH2")), userInfos, attributesResolverMap.get(registrationId).getUsernameKey());
        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(oAuth2User, null, registrationId);
        authenticationToken.setDetails(userInfo);
        return authenticationToken;
    }

    public JSONObject getAccessTokenInfo(String authorizationCode,
                                         String clientId,
                                         String clientSecret,
                                         String redirectUri,
                                         String accessTokenUri) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("client_id", clientId);
        param.add("client_secret", clientSecret);
        param.add("code", authorizationCode);
        param.add("grant_type", "authorization_code");
        param.add("redirect_uri", redirectUri);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(param, headers);
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(accessTokenUri, request , JSONObject.class);
        return response.getBody();
    }

    public JSONObject getUserInfo(String userInfoUri, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", "Bearer " + accessToken);
        HttpEntity request = new HttpEntity(httpHeaders);
        return restTemplate.exchange(userInfoUri, HttpMethod.GET, request, JSONObject.class).getBody();
    }

    public void setClientProperties(OAuth2ClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }

}
