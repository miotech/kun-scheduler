package com.miotech.kun.security.model.bo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthCallbackInformation {

    private String url;

    private String clientId;

    private String responseType;

    private String redirectUri;

}