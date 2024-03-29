package com.miotech.kun.workflow.web;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.pubsub.subscribe.NopEventSubscriber;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.workflow.LocalScheduler;
import com.miotech.kun.workflow.TaskRunStateMachineDispatcher;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunStatistic;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventPublisher;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventSubscriber;
import com.miotech.kun.workflow.executor.DispatchExecutor;
import com.miotech.kun.workflow.executor.ExecutorKind;
import com.miotech.kun.workflow.executor.config.DispatchExecutorConfig;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import com.miotech.kun.workflow.executor.kubernetes.KubeExecutorConfig;
import com.miotech.kun.workflow.executor.kubernetes.KubernetesExecutor;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import com.miotech.kun.workflow.executor.local.PublicEventHandler;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import com.miotech.kun.workflow.web.service.RecoverService;
import com.miotech.kun.workflow.web.service.WorkflowServiceFacadeImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lettuce.core.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class KunWorkflowServerModule extends AppModule {

    private final Props props;

    private final Logger logger = LoggerFactory.getLogger(KunWorkflowServerModule.class);

    public KunWorkflowServerModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(EventBus.class).toInstance(new EventBus());
        bind(Scheduler.class).to(LocalScheduler.class);
        bind(TaskGraph.class).to(DatabaseTaskGraph.class);
        bind(RecoverService.class);
        bind(TaskRunStateMachineDispatcher.class);
        bind(TaskRunStatistic.class);
        bind(PublicEventHandler.class);
        bind(WorkflowServiceFacade.class).to(WorkflowServiceFacadeImpl.class);
    }


    @Provides
    public EventPublisher createRedisPublisher() {
        if (props.containsKey("redis.host")) {
            RedisClient redisClient = RedisClient.create(String.format("redis://%s", props.getString("redis.host")));
            return new RedisStreamEventPublisher(props.getString("redis.stream-key"), redisClient);
        }

        return new NopEventPublisher();
    }

    @Provides
    public EventSubscriber createSubscriber(){
        if (props.containsKey("redis.host")) {
            RedisClient redisClient = RedisClient.create(String.format("redis://%s", props.getString("redis.host")));
            return new RedisStreamEventSubscriber(props.getString("redis.stream-key"),
                    props.getString("redis.workflow.group"),
                    props.getString("redis.workflow.consumer"),
                    redisClient);
        }

        return new NopEventSubscriber();
    }

    @Provides
    @Singleton
    public Executor createExecutor(Props props, Injector injector){
        ExecutorKind executorKind = ExecutorKind.valueOf(props.getString("executor.kind").toUpperCase());
        Executor executor = null;
        switch (executorKind){
            case KUBERNETES:
                KubeExecutorConfig kubeExecutorConfig = props.getValue("executor",KubeExecutorConfig.class);
                executor = new KubernetesExecutor(kubeExecutorConfig,"kubeExecutor");
                break;
            case DISPATCH:
                List<ExecutorConfig> executorConfigList = props.getValueList("executor.executors",ExecutorConfig.class);
                String defaultExecutor = props.get("executor.defaultExecutor");
                DispatchExecutorConfig dispatchExecutorConfig = new DispatchExecutorConfig();
                dispatchExecutorConfig.setDefaultExecutor(defaultExecutor);
                dispatchExecutorConfig.setKind(executorKind.name());
                dispatchExecutorConfig.setExecutorConfigList(executorConfigList);
                executor = new DispatchExecutor(dispatchExecutorConfig);
                break;
            default:
                ExecutorConfig executorConfig = props.getValue("executor",ExecutorConfig.class);
                executor = new LocalExecutor(executorConfig);
        }
        executor.injectMembers(injector);
        logger.debug("executor inject member finished");
        return executor;
    }

    @Provides
    @Singleton
    @Named("executorDatasource")
    public DataSource getExecutorDatasource(Props props){
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(props.getInt("datasource.minimumIdle", 10));
        config.setMaximumPoolSize(props.getInt("datasource.maxPoolSize", 30));
        config.setJdbcUrl(props.get("datasource.jdbcUrl"));
        config.setUsername(props.get("datasource.username"));
        config.setPassword(props.get("datasource.password"));
        config.setDriverClassName(props.get("datasource.driverClassName"));
        return new HikariDataSource(config);
    }



}
