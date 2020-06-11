package com.miotech.kun.commons.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EventLoop<T, E> {
    private static final Logger logger = LoggerFactory.getLogger(EventLoop.class);

    private final String name;
    private final int size;
    private final List<Worker> workers;
    private final Map<Thread, Worker> threadMap;
    private final ConcurrentMap<T, Set<Pair<Worker, EventListener<E>>>> registry;
    private final AtomicBoolean stopped;

    public EventLoop(String name, int nThreads) {
        this.name = name;
        this.size = nThreads;
        this.registry = new ConcurrentHashMap<>();
        this.stopped = new AtomicBoolean(false);

        ImmutableMap.Builder<Thread, Worker> mapBuilder = ImmutableMap.builder();
        ImmutableList.Builder<Worker> listBuilder = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            Worker newWorker = new Worker("event-loop-" + name + "-worker-" + i);
            mapBuilder.put(newWorker.getThread(), newWorker);
            listBuilder.add(newWorker);
        }
        this.workers = listBuilder.build();
        this.threadMap = mapBuilder.build();
    }

    public void start() {
        if (stopped.get()) {
            throw new IllegalStateException("event loop " + name + " has already been stopped");
        }

        for (Worker worker : workers) {
            worker.start();
        }
        logger.debug("workers are started. EventLoop={}", name);
    }

    public void stop() {
        if (stopped.compareAndSet(false, true)) {
            for (Worker worker : workers) {
                worker.getThread().interrupt();
            }
            for (Worker worker : workers) {
                try {
                    worker.getThread().join();
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
        int i = key.hashCode() % size;
        Worker worker = workers.get(i);
        logger.debug("dispatch event {} to worker {}. EventLoop={}", event, worker, name);
        worker.post(event, this::onReceive);

        // dispatch to listeners
        Set<Pair<Worker, EventListener<E>>> listeners = registry.get(key);
        if (listeners != null) {
            for (Pair<Worker, EventListener<E>> pair : listeners) {
                worker = pair.getKey();
                EventListener<E> listener = pair.getValue();
                logger.debug("dispatch event {} to worker {} with listener. EventLoop={}", event, worker, name);
                worker.post(event, listener);
            }
        }
    }

    protected void register(T key, EventListener<E> listener) {
        registry.putIfAbsent(key, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        registry.get(key).add(Pair.of(currentWorker(), listener));
    }

    protected void unregister(T key) {
        Worker worker = currentWorker();
        registry.get(key).removeIf(pair -> pair.getKey().equals(worker));
    }

    protected abstract void onReceive(E event);

    private Worker currentWorker() {
        Worker worker = threadMap.get(Thread.currentThread());
        if (worker == null) {
            throw new IllegalStateException("Illegal called outside of worker thread. thread=" + Thread.currentThread());
        }
        return worker;
    }

    private class Worker {
        private final Thread thread;
        private final BlockingQueue<Pair<E, EventListener<E>>> queue;

        public Worker(String name) {
            queue = new LinkedBlockingDeque<>();
            thread = new Thread(name) {
                @Override
                public void run() {
                    while (!stopped.get()) {
                        E event = null;
                        EventListener<E> listener;
                        try {
                            Pair<E, EventListener<E>> e = queue.take();
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
            };
        }

        public void start() {
            thread.start();
        }

        public void post(E event, EventListener<E> listener) {
            queue.offer(Pair.of(event, listener));
        }

        public Thread getThread() {
            return thread;
        }

        public boolean isIdle() {
            return queue.isEmpty();
        }

        @Override
        public String toString() {
            return "Worker[" + thread.getName() + "]";
        }
    }

    @FunctionalInterface
    public interface EventListener<E> {
        void onReceive(E event);
    }
}
