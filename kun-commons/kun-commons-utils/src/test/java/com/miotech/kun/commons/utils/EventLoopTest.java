package com.miotech.kun.commons.utils;

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class EventLoopTest {
    private static final String REGISTER = "REGISTER";
    private static final String UNREGISTER = "UNREGISTER";
    private static final Integer MAGIC_NUMBER = 42;

    @Test
    public void testAddConsumers_already_added() {
        // prepare
        EventLoop<Integer, String> eventLoop = new EventLoop<>("test");
        eventLoop.addConsumers(Collections.emptyList());

        // process
        try {
            eventLoop.addConsumers(Collections.emptyList());
            fail();
        } catch (Exception exception) {
            assertThat(exception, instanceOf(IllegalStateException.class));
        }
    }

    @Test
    public void testOnReceive_post_to_single_worker() {
        // prepare
        EchoEventLoop eventLoop = new EchoEventLoop(4);
        eventLoop.start();

        // process
        eventLoop.post(1, "1");
        eventLoop.post(1, "2");

        // verify
        await().atMost(5, TimeUnit.SECONDS).until(eventLoop::isIdle);

        ConcurrentMap<Thread, ConcurrentLinkedQueue<String>> received = eventLoop.getReceived();
        assertThat(received.size(), is(1));
        assertThat(getOnlyValue(received), contains("1", "2"));

        // teardown
        eventLoop.stop();
    }

    @Test
    public void testOnReceive_post_to_different_workers() {
        // prepare
        EchoEventLoop eventLoop = new EchoEventLoop(4);
        eventLoop.start();

        // process
        eventLoop.post(1, "1");
        eventLoop.post(2, "2");

        // verify
        await().atMost(5, TimeUnit.SECONDS).until(eventLoop::isIdle);

        ConcurrentMap<Thread, ConcurrentLinkedQueue<String>> received = eventLoop.getReceived();
        assertThat(received.size(), is(2));
        for (Thread t : received.keySet()) {
            assertThat(received.get(t), anyOf(contains("1"), contains("2")));
        }

        // teardown
        eventLoop.stop();
    }

    @Test
    public void testOnReceive_post_register_listener() {
        // prepare
        EchoEventLoop eventLoop = new EchoEventLoop(4);
        eventLoop.start();

        // process
        eventLoop.post(MAGIC_NUMBER, "1");        // thread1: ["1"]
        eventLoop.post(MAGIC_NUMBER + 1, REGISTER); // thread2: ["REGISTER"]
        await().until(eventLoop::isIdle);               // await until event consumed
        eventLoop.post(MAGIC_NUMBER, "2");        // thread1: ["1", "2"], thread2: ["REGISTER", "2"]

        // verify
        await().atMost(5, TimeUnit.SECONDS).until(eventLoop::isIdle);

        ConcurrentMap<Thread, ConcurrentLinkedQueue<String>> received = eventLoop.getReceived();
        assertThat(received.size(), is(2));
        for (Thread t : received.keySet()) {
            assertThat(received.get(t), anyOf(contains("1", "2"), contains(REGISTER, "2")));
        }

        // teardown
        eventLoop.stop();
    }

    @Test
    public void testOnReceive_post_unregister_listener() {
        // prepare
        EchoEventLoop eventLoop = new EchoEventLoop(4);
        eventLoop.start();

        // process
        eventLoop.post(MAGIC_NUMBER, "1");           // thread1: ["1"]
        eventLoop.post(MAGIC_NUMBER + 1, REGISTER);   // thread2: ["REGISTER"]
        await().until(eventLoop::isIdle);                  // await until event consumed
        eventLoop.post(MAGIC_NUMBER, "2");           // thread1: ["1", "2"], thread2: ["REGISTER", "2"]
        eventLoop.post(MAGIC_NUMBER + 1, UNREGISTER); // thread1: ["1", "2"], thread2: ["REGISTER", "2", "UNREGISTER"]
        await().until(eventLoop::isIdle);                  // await until event consumed
        eventLoop.post(MAGIC_NUMBER, "3");           // thread1: ["1", "2", "3"], thread2: ["REGISTER", "2", "UNREGISTER"]

        // verify
        await().atMost(5, TimeUnit.SECONDS).until(eventLoop::isIdle);

        ConcurrentMap<Thread, ConcurrentLinkedQueue<String>> received = eventLoop.getReceived();
        assertThat(received.size(), is(2));
        for (Thread t : received.keySet()) {
            assertThat(received.get(t), anyOf(contains("1", "2", "3"), contains(REGISTER, "2", UNREGISTER)));
        }

        // teardown
        eventLoop.stop();
    }
    
    @Test
    public void testOnReceive_stopped() {
        // prepare
        EchoEventLoop eventLoop = new EchoEventLoop(4);
        eventLoop.start();

        // process
        eventLoop.post(1, "1");
        await().until(eventLoop::isIdle);
        eventLoop.stop();
        eventLoop.post(1, "2");
        eventLoop.post(1, "3");

        // verify
        await().atMost(5, TimeUnit.SECONDS).until(eventLoop::isIdle);

        ConcurrentMap<Thread, ConcurrentLinkedQueue<String>> received = eventLoop.getReceived();
        assertThat(received.size(), is(1));
        assertThat(getOnlyValue(received), contains("1"));

        // teardown
        eventLoop.stop();
    }

    private static class EchoEventLoop extends EventLoop<Integer, String> {
        private final ConcurrentMap<Thread, ConcurrentLinkedQueue<String>> received = new ConcurrentHashMap<>();

        public EchoEventLoop(int n) {
            super("echo");
            addConsumers(IntStream.range(0, n)
                    .mapToObj((i) -> new EchoEventConsumer())
                    .collect(Collectors.toList()));
        }

        private void addEvent(String event) {
            Thread self = Thread.currentThread();
            received.putIfAbsent(self, new ConcurrentLinkedQueue<>());
            received.get(self).add(event);
        }

        public ConcurrentMap<Thread, ConcurrentLinkedQueue<String>> getReceived() {
            return received;
        }

        private class EchoEventConsumer extends EventConsumer<Integer, String> {
            @Override
            public void onReceive(String event) {
                addEvent(event);

                if (REGISTER.equals(event)) {
                    listenTo(MAGIC_NUMBER, EchoEventLoop.this::addEvent);
                } else if (UNREGISTER.equals(event)) {
                    unlistenTo(MAGIC_NUMBER);
                }
            }
        }
    }

    private <V> V getOnlyValue(Map<?, V> map) {
        return map.get(Iterables.getOnlyElement(map.keySet()));
    }
}