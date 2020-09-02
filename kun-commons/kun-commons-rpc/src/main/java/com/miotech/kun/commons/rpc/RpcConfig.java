package com.miotech.kun.commons.rpc;

public class RpcConfig {
    private String applicationName;

    private String registryCenterURL;

    private String registryCenterUsername;

    private String registryCenterPassword;

    private int port;

    public RpcConfig(String applicationName) {
        this.applicationName = applicationName;
        // By default we use redis as registry center
        this.registryCenterURL = "redis://127.0.0.1:6379";
        this.registryCenterUsername = "";
        this.registryCenterPassword = "";
    }

    public RpcConfig(String applicationName, String registryCenterURL) {
        this(applicationName);
        this.registryCenterURL = registryCenterURL;
        this.registryCenterUsername = "";
        this.registryCenterPassword = "";
    }

    public RpcConfig(String applicationName, String registryCenterURL, String registryCenterUsername, String registryCenterPassword) {
        this(applicationName, registryCenterURL);
        this.registryCenterUsername = registryCenterUsername;
        this.registryCenterPassword = registryCenterPassword;
    }

    public RpcConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public int getPort() {
        return this.port;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public String getRegistryCenterURL() {
        return registryCenterURL;
    }

    public String getRegistryCenterUsername() {
        return registryCenterUsername;
    }

    public String getRegistryCenterPassword() {
        return registryCenterPassword;
    }
}
