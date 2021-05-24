package com.miotech.kun.workflow.web;

import com.google.common.eventbus.EventBus;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.annotation.BasePackageScan;
import com.miotech.kun.commons.web.module.KunWebServerModule;
import com.miotech.kun.workflow.LocalScheduler;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.core.publish.NopEventPublisher;
import com.miotech.kun.workflow.core.publish.RedisEventPublisher;
import com.miotech.kun.workflow.executor.WorkerFactory;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import com.miotech.kun.workflow.executor.local.LocalWorkerFactory;
import com.miotech.kun.workflow.executor.rpc.LocalExecutorFacadeImpl;
import com.miotech.kun.workflow.executor.rpc.WorkerClusterConsumer;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.web.service.InitService;
import com.miotech.kun.workflow.web.service.RecoverService;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class KunWorkflowServerModule extends KunWebServerModule {

    private final Props props;

    public KunWorkflowServerModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(Executor.class).to(LocalExecutor.class);
        bind(WorkerFactory.class).to(LocalWorkerFactory.class);
        bind(WorkflowExecutorFacade.class).to(LocalExecutorFacadeImpl.class);
        bind(EventBus.class).toInstance(new EventBus());
        bind(Scheduler.class).to(LocalScheduler.class);
        bind(TaskGraph.class).to(DatabaseTaskGraph.class);
        bind(InitService.class);
        bind(RecoverService.class);
    }

    @Provides
    @Singleton
    @BasePackageScan
    public String getPackageScan() {
        return this.getClass().getPackage().getName();
    }

    @Singleton
    @Provides
    public WorkerClusterConsumer workerRpcConsumer() {
        return new WorkerClusterConsumer();
    }


    @Singleton
    @Provides
    public WorkflowWorkerFacade workerFacade(WorkerClusterConsumer workerClusterConsumer){
        return workerClusterConsumer.getService("default", WorkflowWorkerFacade.class, "1.0");
    }

    @Provides
    public EventPublisher createRedisPublisher() {
        if (props.containsKey("redis.host")) {
            JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), props.getString("redis.host"));
            return new RedisEventPublisher(props.getString("redis.notify-channel"), jedisPool);
        }

        return new NopEventPublisher();
    }

}
