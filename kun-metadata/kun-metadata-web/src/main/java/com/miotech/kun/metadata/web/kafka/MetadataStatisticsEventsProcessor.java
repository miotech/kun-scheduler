package com.miotech.kun.metadata.web.kafka;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.event.MetadataStatisticsEvent;
import com.miotech.kun.metadata.databuilder.constant.OperatorKey;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
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
public class MetadataStatisticsEventsProcessor extends EventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MetadataStatisticsEventsProcessor.class);

    private Props props;
    private WorkflowServiceFacade workflowServiceFacade;

    @Inject
    public MetadataStatisticsEventsProcessor(Props props, WorkflowServiceFacade workflowServiceFacade) {
        this.props = props;
        this.workflowServiceFacade = workflowServiceFacade;
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
                logger.debug("topic = {}, value = {}, offset = {}", record.topic(), record.value(), record.offset());
                try {
                    MetadataStatisticsEvent mse = JSONUtils.jsonToObject(record.value(), MetadataStatisticsEvent.class);

                    workflowServiceFacade.executeTask(props.getLong(TaskParam.MSE_TASK.getName()), buildVariablesForTaskRun(mse.getGid(), mse.getSnapshotId(), mse.getEventType()));
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

    private Map<String, Object> buildVariablesForTaskRun(long gid, long snapshotId, MetadataStatisticsEvent.EventType eventType) {
        Map<String, Object> conf = Maps.newHashMap();
        conf.put(OperatorKey.DATASOURCE_JDBC_URL, props.get(PropKey.JDBC_URL));
        conf.put(OperatorKey.DATASOURCE_USERNAME, props.get(PropKey.USERNAME));
        conf.put(OperatorKey.DATASOURCE_PASSWORD, props.get(PropKey.PASSWORD));
        conf.put(OperatorKey.DATASOURCE_DRIVER_CLASS_NAME, props.get(PropKey.DRIVER_CLASS_NAME));
        conf.put(OperatorKey.GID, String.valueOf(gid));
        conf.put(OperatorKey.SNAPSHOT_ID, String.valueOf(snapshotId));
        conf.put(OperatorKey.STATISTICS_MODE, eventType.name());

        return conf;
    }

}
