package com.miotech.kun.metadata.web.kafka;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.client.WorkflowClient;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Singleton
public class MetadataStatEventsProcessor extends EventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MetadataStatEventsProcessor.class);

    private Props props;
    private WorkflowClient workflowClient;

    @Inject
    public MetadataStatEventsProcessor(Props props, WorkflowClient workflowClient) {
        this.props = props;
        this.workflowClient = workflowClient;
    }

    @Override
    public void consume() {
        Properties properties = generateGeneralConsumerProperties();
        properties.put("bootstrap.servers", props.getString("kafka.bootstrapServers"));
        properties.put("group.id", props.getString("kafka.mseGroupId"));

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(props.getString("kafka.mseTopicName")));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            records.forEach(record -> {
                logger.debug("topic = {}, value = {}", record.topic(), record.value());
                try {
                    workflowClient.executeTask(props.getLong(TaskParam.MSE_TASK.getName()), buildVariablesForTaskRun(record.value()));
                } catch (Exception e) {
                    logger.error("MSE Processor Error", e);
                    logger.error("Message: {}", record);
                }

            });
        }
    }

    public void start() {
        new Thread(() -> consume()).start();
    }

    private Map<String, Object> buildVariablesForTaskRun(String gid) {
        Map<String, Object> conf = Maps.newHashMap();
        conf.put(PropKey.JDBC_URL, props.get(PropKey.JDBC_URL));
        conf.put(PropKey.USERNAME, props.get(PropKey.USERNAME));
        conf.put(PropKey.PASSWORD, props.get(PropKey.PASSWORD));
        conf.put(PropKey.DRIVER_CLASS_NAME, props.get(PropKey.DRIVER_CLASS_NAME));
        conf.put(PropKey.GID, gid);

        return conf;
    }

}
