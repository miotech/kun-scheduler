package com.miotech.kun.metadata.web.kafka;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.constant.DataBuilderDeployMode;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.metadata.web.kafka.model.MetadataChangeEvent;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Singleton
public class MetadataChangeEventsProcessor extends EventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MetadataChangeEventsProcessor.class);

    private Props props;
    private WorkflowClient workflowClient;

    @Inject
    public MetadataChangeEventsProcessor(Props props, WorkflowClient workflowClient) {
        this.props = props;
        this.workflowClient = workflowClient;
    }

    @Override
    public void consume() {
        Properties properties = generateGeneralConsumerProperties();
        properties.put("bootstrap.servers", props.getString("kafka.bootstrapServers"));
        properties.put("group.id", props.getString("kafka.mceGroupId"));

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(props.getString("kafka.mceTopicName")));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            records.forEach(record -> {
                logger.debug("topic = {}, value = {}", record.topic(), record.value());
                try {
                    MetadataChangeEvent mce = JSONUtils.jsonToObject(record.value(), MetadataChangeEvent.class);

                    workflowClient.executeTask(props.getLong(TaskParam.MCE_TASK.getName()), buildVariablesForTaskRun(mce));
                } catch (Exception e) {
                    logger.error("MCE Processor Error", e);
                    logger.error("Message: {}", record);
                }
            });
        }
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
        conf.put(PropKey.BROKERS, props.getString("kafka.bootstrapServers"));
        conf.put(PropKey.MSE_TOPIC, props.getString("kafka.mseTopicName"));

        return conf;
    }

}
