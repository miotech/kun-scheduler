package com.miotech.kun.workflow.core.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

@Singleton
public class RedisEventPublisher implements EventPublisher{

    private static Logger logger = LoggerFactory.getLogger(RedisEventPublisher.class);

    private Jedis publisherJedis;

    private String channel;

    public RedisEventPublisher(String channel, Jedis jedis){
        this.channel = channel;
        this.publisherJedis = jedis;
    }

    @Override
    public void publish(Event event) {
        try {
            publisherJedis.publish(channel, EventMapper.toJson(event));
        } catch (JsonProcessingException e) {
            logger.error("redis publish failed due to event to json string", e);
        }catch (Exception e){
            logger.error("redis publish failed", e);
        }
    }
}
