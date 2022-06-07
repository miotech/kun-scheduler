package com.miotech.kun.workflow.core.publish;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.event.EventReceiver;
import com.miotech.kun.commons.pubsub.event.PrivateEvent;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventPublisher;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventSubscriber;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class RedisStreamPubSubTest {

    private final static String IMAGE_NAME = "redis:6.2";
    private final static String STREAM_KEY = "kun";
    private final static String GROUP = "test-group";
    private final static String CONSUMER = "test-consumer";

    protected static GenericContainer redisContainer;

    static {
        redisContainer = new GenericContainer(DockerImageName.parse(IMAGE_NAME))
                .withExposedPorts(6379);
        redisContainer.start();
    }

    @Test
    public void testPubSub() {
        EventReceiver eventReceiver = Mockito.mock(EventReceiver.class);
        RedisClient redisClient = RedisClient.create(String.format("redis://%s:%s", redisContainer.getHost(), redisContainer.getMappedPort(6379)));

        RedisStreamEventSubscriber subscriber =  new RedisStreamEventSubscriber(STREAM_KEY, GROUP, CONSUMER, redisClient);
        subscriber.subscribe(eventReceiver);

        RedisStreamEventPublisher publisher = new RedisStreamEventPublisher(STREAM_KEY, redisClient);
        RandomMockEvent randomMockEvent = new RandomMockEvent("test_randon_event");
        publisher.publish(randomMockEvent);

        verify(eventReceiver, timeout(5000).times(1)).onReceive(any());
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventReceiver).onReceive(captor.capture());
        RandomMockEvent randomMockEventOfReceive = (RandomMockEvent) captor.getValue();
        assertThat(randomMockEventOfReceive.getName(), is(randomMockEvent.getName()));
    }

    private static class RandomMockEvent extends PrivateEvent {
        private final String name;

        @JsonCreator
        public RandomMockEvent(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
