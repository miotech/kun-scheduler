package com.miotech.kun.workflow.testing.event;

import com.google.common.eventbus.Subscribe;
import com.miotech.kun.workflow.core.event.Event;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class EventCollector {
    private final LinkedList<Event> events = new LinkedList<>();

    @Subscribe
    public void onReceive(Event event) {
        events.add(event);
    }

    public List<Event> getEvents() {
        return events;
    }

    public Optional<Event> getLastEvent() {
        return Optional.ofNullable(events.peekLast());
    }
}
