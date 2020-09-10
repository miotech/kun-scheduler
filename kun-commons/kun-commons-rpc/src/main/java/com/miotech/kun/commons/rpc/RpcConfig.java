package com.miotech.kun.commons.rpc;

import org.apache.dubbo.config.ServiceConfig;

import java.util.LinkedList;
import java.util.List;

public class RpcConfig {
    private String applicationName;

    private String registryCenterURL;

    private String registryCenterUsername;

    private String registryCenterPassword;

    private int port;

    private final List<ServiceConfig> services = new LinkedList<>();

    public RpcConfig(String applicationName) {
        this.applicationName = applicationName;
        this.registryCenterURL = "";
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

    public <T> RpcConfig addService(Class<T> interfaceClass, String version, T impl) {
        ServiceConfig<T> service = new ServiceConfig<>();
        service.setInterface(interfaceClass);
        service.setVersion(version);
        service.setRef(impl);
        services.add(service);
        return this;
    }

    public List<ServiceConfig> getServices() {
        return this.services;
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
