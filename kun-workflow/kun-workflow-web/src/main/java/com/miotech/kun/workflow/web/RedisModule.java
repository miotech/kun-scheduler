package com.miotech.kun.workflow.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.core.publish.RedisEventPublisher;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisModule extends AbstractModule {
    private final Props props;

    public RedisModule(Props props) {
        this.props = props;
    }

    @Provides
    public EventPublisher createRedisPublisher() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), props.getString("redis.host"));
        return new RedisEventPublisher(props.getString("redis.notify-channel"), jedisPool);
    }
}
