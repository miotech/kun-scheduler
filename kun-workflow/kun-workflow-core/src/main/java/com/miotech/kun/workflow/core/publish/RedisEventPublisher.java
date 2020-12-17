package com.miotech.kun.workflow.core.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Singleton
public class RedisEventPublisher implements EventPublisher{

    private static Logger logger = LoggerFactory.getLogger(RedisEventPublisher.class);

    private JedisPool jedisPool;

    private String channel;

    public RedisEventPublisher(String channel, JedisPool jedisPool){
        this.channel = channel;
        this.jedisPool = jedisPool;
    }

    @Override
    public void publish(Event event) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.publish(channel, EventMapper.toJson(event));
        } catch (JsonProcessingException e) {
            logger.error("redis publish failed due to event to json string", e);
        }catch (Exception e){
            logger.error("redis publish failed", e);
        }finally {
            if(jedis != null)
                jedis.close();
        }
    }
}
