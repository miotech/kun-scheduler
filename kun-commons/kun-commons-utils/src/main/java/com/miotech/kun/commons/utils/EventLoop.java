package com.miotech.kun.commons.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventLoop<T, E> {
    private static final Logger logger = LoggerFactory.getLogger(EventLoop.class);

    private final String name;
    private final AtomicBoolean stopped;
    private int size;
    private List<Worker> workers;
    private Map<Thread, Worker> threadMap;
    private ConcurrentMap<T, Set<Pair<Worker, EventHandler<E>>>> registry;

    public EventLoop(String name) {
        this.name = name;
        this.stopped = new AtomicBoolean(false);
    }

    public void addConsumers(List<EventConsumer<T, E>> consumers) {
        if (this.workers != null) {
            throw new IllegalStateException("consumers are already added.");
        }

        this.size = consumers.size();
        this.registry = new ConcurrentHashMap<>();

        ImmutableMap.Builder<Thread, Worker> mapBuilder = ImmutableMap.builder();
        ImmutableList.Builder<Worker> listBuilder = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            EventConsumer<T, E> consumer = consumers.get(i);
            Worker newWorker = new Worker(
                    "event-loop-" + name + "-worker-" + i,
                    consumer);
            consumer.setEventLoop(this);
            mapBuilder.put(newWorker, newWorker);
            listBuilder.add(newWorker);
        }
        this.workers = listBuilder.build();
        this.threadMap = mapBuilder.build();
    }

    public void start() {
        if (stopped.get()) {
            throw new IllegalStateException("event loop " + name + " has already been stopped");
        }

        if (workers == null) {
            throw new IllegalStateException("consumers have not been added.");
        }

        for (Worker worker : workers) {
            worker.start();
        }
        logger.debug("workers are started. EventLoop={}", name);
    }

    public void stop() {
        if (stopped.compareAndSet(false, true)) {
            for (Worker worker : workers) {
                worker.interrupt();
            }
            for (Worker worker : workers) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public boolean isIdle() {
        for (Worker worker : workers) {
            if (!worker.isIdle()) {
                return false;
            }
        }
        return true;
    }

    public void post(T key, E event) {
        if (stopped.get()) {
            logger.debug("discard event as current event loop is stopped. Event={}, EventLoop={}", event, name);
            return;
        }
        logger.debug("receive event {} of key {}. EventLoop={}", event, key, name);
        dispatch(key, event);
    }

    private void dispatch(T key, E event) {
        int i = (key.hashCode() & 0x7fffffff) % size;

        // dispatch to workder (default handler)
        Worker worker = workers.get(i);
        logger.debug("dispatch event {} to worker {}. EventLoop={}", event, worker, name);
        worker.post(event);

        // dispatch to listeners
        Set<Pair<Worker, EventHandler<E>>> listeners = registry.get(key);
        if (listeners != null) {
            for (Pair<Worker, EventHandler<E>> pair : listeners) {
                worker = pair.getKey();
                EventHandler<E> listener = pair.getValue();
                logger.debug("dispatch event {} to worker {} with listener. EventLoop={}", event, worker, name);
                worker.post(event, listener);
            }
        }
    }

    protected void register(T key, EventHandler<E> listener) {
        registry.putIfAbsent(key, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        registry.get(key).add(Pair.of(currentWorker(), listener));
    }

    protected void unregister(T key) {
        Worker worker = currentWorker();
        registry.get(key).removeIf(pair -> pair.getKey().equals(worker));
    }

    private Worker currentWorker() {
        Worker worker = threadMap.get(Thread.currentThread());
        if (worker == null) {
            throw new IllegalStateException("Illegal called outside of worker thread. thread=" + Thread.currentThread());
        }
        return worker;
    }

    private class Worker extends Thread {
        private final EventConsumer<T, E> consumer;
        private final BlockingQueue<Pair<E, EventHandler<E>>> queue;

        public Worker(String name, EventConsumer<T, E> consumer) {
            super(name);
            this.consumer = consumer;
            this.queue = new LinkedBlockingDeque<>();
        }

        @Override
        public void run() {
            while (!stopped.get()) {
                E event = null;
                EventHandler<E> listener;
                try {
                    Pair<E, EventHandler<E>> e = queue.take();
                    listener = e.getValue();
                    event = e.getKey();
                    listener.onReceive(event);
                } catch (InterruptedException interrupted) {
                    logger.debug("worker is interrupted. EventLoop={}", name);
                    break;
                } catch (Throwable e) {
                    logger.error("failed to handle event. Event={}", event, e);
                }
            }
        }

        public void post(E event) {
            queue.offer(Pair.of(event, consumer));
        }

        public void post(E event, EventHandler<E> listener) {
            queue.offer(Pair.of(event, listener));
        }

        public boolean isIdle() {
            return queue.isEmpty();
        }

        @Override
        public String toString() {
            return "Worker[" + getName() + "]";
        }
    }
}
