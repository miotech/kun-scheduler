/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.miotech.kun.commons.rpctest;

import com.google.common.collect.Lists;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.MethodConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ArgumentBuilder;
import org.apache.dubbo.config.bootstrap.builders.RegistryBuilder;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.RegistryService;
import org.apache.dubbo.rpc.RpcContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * SimpleRegistryService
 *
 */
public class SimpleRegistryService extends AbstractRegistryService {

    private final static Logger logger = LoggerFactory.getLogger(SimpleRegistryService.class);
    private final ConcurrentMap<String, ConcurrentMap<String, URL>> remoteRegistered = new ConcurrentHashMap<String, ConcurrentMap<String, URL>>();
    private final ConcurrentMap<String, ConcurrentMap<String, NotifyListener>> remoteListeners = new ConcurrentHashMap<String, ConcurrentMap<String, NotifyListener>>();
    private List<String> registries;
    private Integer port;

    public SimpleRegistryService() {
        port = null;
    }

    public SimpleRegistryService(int port) {
        this.port = port;
    }

    @Override
    public void register(String service, URL url) {
        if (logger.isInfoEnabled()) {
            logger.info("[register] service: " + service + ", URL: " + url);
        }
        super.register(service, url);
        String client = RpcContext.getContext().getRemoteAddressString();
        Map<String, URL> urls = remoteRegistered.computeIfAbsent(client, k -> new ConcurrentHashMap<>());
        urls.put(service, url);
        notify(service, getRegistered().get(service));
    }

    @Override
    public void unregister(String service, URL url) {
        if (logger.isInfoEnabled()) {
            logger.info("[unregister] service: " + service + ", URL: " + url);
        }
        super.unregister(service, url);
        String client = RpcContext.getContext().getRemoteAddressString();
        Map<String, URL> urls = remoteRegistered.get(client);
        if (urls != null && urls.size() > 0) {
            urls.remove(service);
        }
        notify(service, getRegistered().get(service));
    }

    @Override
    public void subscribe(String service, URL url, NotifyListener listener) {
        String client = RpcContext.getContext().getRemoteAddressString();
        if (logger.isInfoEnabled()) {
            logger.info("[subscribe] service: " + service + ",client:" + client);
        }
        List<URL> urls = getRegistered().get(service);
        if ((RegistryService.class.getName() + ":0.0.0").equals(service)
                && (urls == null || urls.size() == 0)) {
            register(service, new URL("dubbo",
                    NetUtils.getLocalHost(),
                    getPort(),
                    org.apache.dubbo.registry.RegistryService.class.getName(),
                    url.getParameters()));
            List<String> rs = registries;
            if (rs != null && rs.size() > 0) {
                for (String registry : rs) {
                    register(service, UrlUtils.parseURL(registry, url.getParameters()));
                }
            }
        }
        super.subscribe(service, url, listener);

        Map<String, NotifyListener> listeners = remoteListeners.computeIfAbsent(client, k -> new ConcurrentHashMap<>());
        listeners.put(service, listener);
        urls = getRegistered().get(service);
        if (urls != null && urls.size() > 0) {
            listener.notify(urls);
        }
    }

    @Override
    public void unsubscribe(String service, URL url, NotifyListener listener) {
        super.unsubscribe(service, url, listener);
        if (logger.isInfoEnabled()) {
            logger.info("[unsubscribe] service: " + service + ", URL: " + url);
        }
        String client = RpcContext.getContext().getRemoteAddressString();
        Map<String, NotifyListener> listeners = remoteListeners.get(client);
        if (listeners != null && listeners.size() > 0) {
            listeners.remove(service);
        }
        List<URL> urls = getRegistered().get(service);
        if (urls != null && urls.size() > 0) {
            listener.notify(urls);
        }
    }

    public void disconnect() {
        String client = RpcContext.getContext().getRemoteAddressString();
        if (logger.isInfoEnabled()) {
            logger.info("Disconnected " + client);
        }
        ConcurrentMap<String, URL> urls = remoteRegistered.get(client);
        if (urls != null && urls.size() > 0) {
            for (Map.Entry<String, URL> entry : urls.entrySet()) {
                super.unregister(entry.getKey(), entry.getValue());
            }
        }
        Map<String, NotifyListener> listeners = remoteListeners.get(client);
        if (listeners != null && listeners.size() > 0) {
            for (Map.Entry<String, NotifyListener> entry : listeners.entrySet()) {
                String service = entry.getKey();
                super.unsubscribe(service, new URL("subscribe",
                        RpcContext.getContext().getRemoteHost(),
                        RpcContext.getContext().getRemotePort(),
                        org.apache.dubbo.registry.RegistryService.class.getName(), getSubscribed(service)), entry.getValue());
            }
        }
    }

    public List<String> getRegistries() {
        return registries;
    }

    public void setRegistries(List<String> registries) {
        this.registries = registries;
    }

    public String getRegistryURL() {
        return "dubbo://" + NetUtils.getLocalHost() + ":" + getPort();
    }

    public int getPort() {
        return Objects.nonNull(port) ? port : RpcContext.getContext().getLocalPort();
    }

    public DubboBootstrap start() {
        // Application config
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("simple-registry");

        // Protocol config
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setPort(getPort());

        // Service config
        ServiceConfig<SimpleRegistryService> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterface(RegistryService.class.getName());
        serviceConfig.setRef(this);
        serviceConfig.setRegistry(RegistryBuilder.newBuilder().address("N/A").build());  // N/A
        serviceConfig.setOndisconnect("disconnect");

        MethodConfig subscribeMethod = new MethodConfig();
        subscribeMethod.setName("subscribe");
        subscribeMethod.setArguments(Lists.newArrayList(new ArgumentBuilder().index(1).callback(true).build()));

        MethodConfig unsubscribeMethod = new MethodConfig();
        unsubscribeMethod.setName("unsubscribe");
        unsubscribeMethod.setArguments(Lists.newArrayList(new ArgumentBuilder().index(1).callback(false).build()));

        serviceConfig.setMethods(Lists.newArrayList(subscribeMethod, unsubscribeMethod));
        serviceConfig.setCallbacks(1000);

        // Boot up
        logger.debug("Starting SimpleRegistryService...");
        return DubboBootstrap.getInstance()
                .application(applicationConfig)
                .protocol(protocolConfig)
                .service(serviceConfig)
                .start();
    }
}
