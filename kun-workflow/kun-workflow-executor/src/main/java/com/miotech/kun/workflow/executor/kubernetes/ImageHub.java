package com.miotech.kun.workflow.executor.kubernetes;

public class ImageHub {

    private String url;
    private Boolean useSecret;
    private String secret;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getUseSecret() {
        return useSecret;
    }

    public void setUseSecret(Boolean useSecret) {
        this.useSecret = useSecret;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
