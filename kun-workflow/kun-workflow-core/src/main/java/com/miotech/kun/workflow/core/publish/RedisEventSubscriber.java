package com.miotech.kun.workflow.core.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;


public class RedisEventSubscriber implements EventSubscriber {
    private static Logger logger = LoggerFactory.getLogger(RedisEventSubscriber.class);

    private final String channel;

    private final Jedis jedis;

   public RedisEventSubscriber(String channel, Jedis jedis){
       this.channel = channel;
       this.jedis = jedis;
   }

    @Override
    public void subscribe(EventReceiver receiver) {
        RedisMsgPubSubListener listener = new RedisMsgPubSubListener(receiver);
        jedis.subscribe(listener, this.channel);
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
                logger.error("parse subscribed message failed", e);
                logger.error(message);
            }
            eventReceiver.onReceive(event);
        }
    }
}
