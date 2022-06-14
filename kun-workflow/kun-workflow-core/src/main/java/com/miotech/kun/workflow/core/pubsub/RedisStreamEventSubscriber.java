package com.miotech.kun.workflow.core.pubsub;

import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.event.EventReceiver;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.workflow.core.event.EventMapper;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

import static com.miotech.kun.workflow.core.pubsub.RedisStreamEventPublisher.MESSAGE_KEY;


public class RedisStreamEventSubscriber implements EventSubscriber {
    private static Logger logger = LoggerFactory.getLogger(RedisStreamEventSubscriber.class);

    private final String streamKey;
    private final String group;
    private final String consumer;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;


    public RedisStreamEventSubscriber(String streamKey, String group, String consumer, RedisClient redisClient) {
        this.streamKey = streamKey;
        this.group = group;
        this.consumer = consumer;
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
    }

    @Override
    public void subscribe(EventReceiver receiver) {
        RedisSubscriberThread redisSubscriberThread = new RedisSubscriberThread(receiver);
        redisSubscriberThread.start();
    }

    private class RedisSubscriberThread extends Thread {
        private EventReceiver eventReceiver;

        public RedisSubscriberThread(EventReceiver eventReceiver) {
            this.eventReceiver = eventReceiver;
        }

        @Override
        public void run() {
            RedisCommands<String, String> redisCommands = connection.sync();
            try {
                redisCommands.xgroupCreate(XReadArgs.StreamOffset.latest(streamKey), group, XGroupCreateArgs.Builder.mkstream());
            } catch (RedisBusyException redisBusyException) {
                logger.debug("group: {} already exists", group);
            }

            while (true) {
                List<StreamMessage<String, String>> messages = redisCommands.xreadgroup(
                        Consumer.from(group, consumer),
                        XReadArgs.Builder.block(Duration.ofSeconds(10)),
                        XReadArgs.StreamOffset.lastConsumed(streamKey));
                if (CollectionUtils.isEmpty(messages)) {
                    continue;
                }

                for (StreamMessage<String, String> message : messages) {
                    String eventJsonStr = message.getBody().get(MESSAGE_KEY);
                    logger.debug("receive message: {}", eventJsonStr);
                    try {
                        Event event = EventMapper.toEvent(eventJsonStr);
                        eventReceiver.onReceive(event);
                    } catch (Throwable e) {
                        logger.error("Failed to process event: {}", eventJsonStr, e);
                    } finally {
                        redisCommands.xack(streamKey, group, message.getId());
                    }
                }
            }
        }
    }
}
