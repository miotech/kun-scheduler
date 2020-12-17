package com.miotech.kun.workflow.core.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;


public class RedisEventSubscriber implements EventSubscriber {
    private static Logger logger = LoggerFactory.getLogger(RedisEventSubscriber.class);

    private final String channel;

    private final JedisPool jedisPool;

   public RedisEventSubscriber(String channel, JedisPool jedisPool){
       this.channel = channel;
       this.jedisPool = jedisPool;
   }

    @Override
    public void subscribe(EventReceiver receiver) {
        RedisMsgPubSubListener listener = new RedisMsgPubSubListener(receiver);
        RedisSubscriberThread redisSubscriberThread = new RedisSubscriberThread(listener);
        redisSubscriberThread.start();
    }

    private static class RedisMsgPubSubListener extends JedisPubSub {
       private EventReceiver eventReceiver;

       public RedisMsgPubSubListener(EventReceiver eventReceiver){
           this.eventReceiver = eventReceiver;
       }

        @Override
        public void onMessage(String channel, String message) {
            Event event = null;
            try {
                event = EventMapper.toEvent(message);
            } catch (JsonProcessingException e) {
                logger.warn("parse subscribed message failed", e);
                logger.warn(message);
            }
            eventReceiver.onReceive(event);
        }
    }

    private class RedisSubscriberThread extends Thread {
       private RedisMsgPubSubListener listener;

       public RedisSubscriberThread(RedisMsgPubSubListener listener){
           this.listener = listener;
       }

       @Override
        public void run(){
           Jedis jedis = null;
           try {
               jedis = jedisPool.getResource();
               jedis.subscribe(listener, channel);
           } catch (Exception e) {
               logger.error(String.format("subscribe to channel %s error", channel), e);
           } finally {
               if (jedis != null) {
                   jedis.close();
               }
           }
       }
    }
}
