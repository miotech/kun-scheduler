package com.miotech.kun.workflow.web;

import com.google.common.eventbus.EventBus;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.workflow.LocalScheduler;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.ResourceManager;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.core.publish.NopEventPublisher;
import com.miotech.kun.workflow.core.publish.RedisEventPublisher;
import com.miotech.kun.workflow.executor.ExecutorBackEnd;
import com.miotech.kun.workflow.executor.WorkerFactory;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.kubernetes.*;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import com.miotech.kun.workflow.executor.local.LocalWorkerFactory;
import com.miotech.kun.workflow.executor.rpc.KubernetesExecutorFacadeImpl;
import com.miotech.kun.workflow.executor.rpc.LocalExecutorFacadeImpl;
import com.miotech.kun.workflow.executor.rpc.WorkerClusterConsumer;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.web.service.RecoverService;
import com.miotech.kun.workflow.web.service.WorkflowServiceFacadeImpl;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class KunWorkflowServerModule extends AppModule {

    private final Props props;

    public KunWorkflowServerModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        String env = props.getString("executor.env.name","local");
        if (env.equals("local")) {
            bind(Executor.class).to(LocalExecutor.class);
            bind(ExecutorBackEnd.class).to(LocalExecutor.class);
            bind(WorkerFactory.class).to(LocalWorkerFactory.class);
            bind(WorkflowExecutorFacade.class).to(LocalExecutorFacadeImpl.class);
        } else if (env.equals("kubernetes")) {
            String masterUrl = props.get("executor.env.url");
            String token = props.getString("executor.env.oauthToken");
            Config config = new ConfigBuilder().withMasterUrl(masterUrl)
                    .withCaCertFile(props.getString("executor.env.caCertFile"))
                    .withOauthToken(token)
                    .build();
            KubernetesClient client = new DefaultKubernetesClient(config);
            bind(KubernetesClient.class).toInstance(client);
            bind(ResourceManager.class).to(KubernetesResourceManager.class);
            bind(WorkerMonitor.class).to(PodEventMonitor.class);
            bind(WorkerLifeCycleManager.class).to(PodLifeCycleManager.class);
            bind(Executor.class).to(KubernetesExecutor.class);
            bind(WorkflowExecutorFacade.class).to(KubernetesExecutorFacadeImpl.class);
        }
        bind(EventBus.class).toInstance(new EventBus());
        bind(Scheduler.class).to(LocalScheduler.class);
        bind(TaskGraph.class).to(DatabaseTaskGraph.class);
        bind(RecoverService.class);
        bind(WorkflowServiceFacade.class).to(WorkflowServiceFacadeImpl.class);
    }


    @Singleton
    @Provides
    public WorkerClusterConsumer workerRpcConsumer() {
        return new WorkerClusterConsumer();
    }


    @Singleton
    @Provides
    public WorkflowWorkerFacade workerFacade(WorkerClusterConsumer workerClusterConsumer) {
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
