package com.miotech.kun.metadata.web.processor;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventSubscriber;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import com.miotech.kun.workflow.utils.JSONUtils;
import io.lettuce.core.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.miotech.kun.metadata.web.constant.PropKey.INFRA_URL;

@Singleton
public class MetadataChangeEventsProcessor implements EventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MetadataChangeEventsProcessor.class);

    private Props props;
    private WorkflowServiceFacade workflowServiceFacade;
    private EventSubscriber subscriber;

    @Inject
    public MetadataChangeEventsProcessor(Props props, WorkflowServiceFacade workflowServiceFacade) {
        this.props = props;
        this.workflowServiceFacade = workflowServiceFacade;
        if (props.containsKey("redis.host") && props.getBoolean("pushMode", false)) {
            this.subscriber = new RedisStreamEventSubscriber(props.getString("redis.metadata-stream-key"),
                    props.getString("redis.workflow.group"),
                    props.getString("redis.workflow.consumer"),
                    RedisClient.create(String.format("redis://%s", props.getString("redis.host"))));
        }
    }

    @Override
    public void consume() {
        if (subscriber == null) {
            logger.warn("MCE Processor stopped because of `pushMode` is off");
            return;
        }

        subscriber.subscribe(event -> {
            if (event instanceof MetadataChangeEvent) {
                MetadataChangeEvent mce = (MetadataChangeEvent) event;
                long taskId = props.getLong(TaskParam.MCE_TASK.getName());
                Map<String, Object> taskConfigs = buildVariablesForTaskRun(mce);
                logger.debug("Prepare to execute task, taskId: {}, taskConfigs: {}", taskId, JSONUtils.toJsonString(taskConfigs));
                TaskRun taskRun = workflowServiceFacade.executeTask(taskId, taskConfigs);
                logger.debug("Execute task success, taskRun: {}", JSONUtils.toJsonString(taskRun));
            }
        });
    }

    public void start() {
        new Thread(() -> consume()).start();
    }

    private Map<String, Object> buildVariablesForTaskRun(MetadataChangeEvent mce) {
        Map<String, Object> conf = Maps.newHashMap();
        conf.put(PropKey.JDBC_URL, props.get(PropKey.JDBC_URL));
        conf.put(PropKey.USERNAME, props.get(PropKey.USERNAME));
        conf.put(PropKey.PASSWORD, props.get(PropKey.PASSWORD));
        conf.put(PropKey.DRIVER_CLASS_NAME, props.get(PropKey.DRIVER_CLASS_NAME));
        conf.put(PropKey.DEPLOY_MODE, DataBuilderDeployMode.PUSH.name());
        conf.put(PropKey.MCE, JSONUtils.toJsonString(mce));
        conf.put(PropKey.REDIS_HOST, props.getString("redis.host"));
        conf.put(PropKey.STREAM_KEY, props.getString("redis.metadata-stream-key"));
        conf.put(PropKey.MSE_URL, props.getString(INFRA_URL) + "/mse/_execute");

        return conf;
    }

}
