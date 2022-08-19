package com.miotech.kun.security.controller;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.model.bo.OAuthCallbackInformation;
import com.miotech.kun.security.service.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kun/api/v1/security/oauth")
public class OAuthController {

    @Autowired
    private OAuthService oAuthService;

    @GetMapping("callback-url")
    public RequestResult<OAuthCallbackInformation> getCallbackInformation() {
        String registrationId = "kun";
        return RequestResult.success(oAuthService.getCallbackInformation(registrationId));
    }

}
