package com.miotech.kun.workflow.web;

import com.google.common.eventbus.EventBus;
import com.google.inject.Provides;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.pubsub.subscribe.NopEventSubscriber;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.workflow.LocalScheduler;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunStatistic;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.pubsub.RedisEventPublisher;
import com.miotech.kun.workflow.core.pubsub.RedisEventSubscriber;
import com.miotech.kun.workflow.TaskRunStateMachine;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import com.miotech.kun.workflow.web.module.DispatchExecutorModule;
import com.miotech.kun.workflow.web.module.KubernetesExecutorModule;
import com.miotech.kun.workflow.web.module.LocalExecutorModule;
import com.miotech.kun.workflow.web.service.RecoverService;
import com.miotech.kun.workflow.web.service.WorkflowServiceFacadeImpl;
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
        String env = props.getString("executor.env.name", "local");
        if (env.equals("local")) {
            install(new LocalExecutorModule(props));
        } else if (env.equals("kubernetes")) {
            install(new KubernetesExecutorModule(props));
        } else if (env.equals("dispatch")) {
            install(new DispatchExecutorModule(props));
        }

        bind(EventBus.class).toInstance(new EventBus());
        bind(Scheduler.class).to(LocalScheduler.class);
        bind(TaskGraph.class).to(DatabaseTaskGraph.class);
        bind(RecoverService.class);
        bind(TaskRunStateMachine.class);
        bind(TaskRunStatistic.class);
        bind(WorkflowServiceFacade.class).to(WorkflowServiceFacadeImpl.class);
    }


    @Provides
    public EventPublisher createRedisPublisher() {
        if (props.containsKey("redis.host")) {
            JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), props.getString("redis.host"));
            return new RedisEventPublisher(props.getString("redis.notify-channel"), jedisPool);
        }

        return new NopEventPublisher();
    }

    @Provides
    public EventSubscriber createSubscriber(){
        if (props.containsKey("redis.host")) {
            JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), props.getString("redis.host"));
            return new RedisEventSubscriber(props.getString("redis.notify-channel"), jedisPool);
        }

        return new NopEventSubscriber();
    }



}
