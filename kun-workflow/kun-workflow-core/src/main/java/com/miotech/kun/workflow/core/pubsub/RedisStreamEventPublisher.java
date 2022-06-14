package com.miotech.kun.workflow.core.pubsub;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.workflow.core.event.EventMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Singleton
public class RedisStreamEventPublisher implements EventPublisher {

    public static final String MESSAGE_KEY = "msg";
    private static Logger logger = LoggerFactory.getLogger(RedisStreamEventPublisher.class);

    private final RedisClient redisClient;

    private final StatefulRedisConnection<String, String> connection;

    private final String streamKey;

    public RedisStreamEventPublisher(String streamKey, RedisClient redisClient){
        this.streamKey = streamKey;
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
    }

    @Override
    public void publish(Event event) {
        try {
            RedisCommands<String, String> redisCommands = connection.sync();
            Map<String, String> message = Maps.newHashMap();
            String eventJsonStr = EventMapper.toJson(event);
            message.put(MESSAGE_KEY, eventJsonStr);
            String messageId = redisCommands.xadd(streamKey, message);
            logger.debug("send msg: {} to stream, messageId: {}", eventJsonStr, messageId);
        } catch (Exception e) {
            logger.error(String.format("redis publish event: %s failed", event), e);
        }
    }
}
