package com.mingri.publisher.impl;

import com.mingri.event.Event;
import com.mingri.manager.EventListenerManager;
import com.mingri.publisher.EventPublisher;



public class DefaultEventPublisher implements EventPublisher {

    private EventListenerManager eventListenerManager;

    public DefaultEventPublisher(EventListenerManager eventListenerManager) {
        this.eventListenerManager = eventListenerManager;
    }

    /**
     * 发布事件
     */
    @Override
    public <E extends Event> void publish(E event) {
        eventListenerManager.notifyListener(event);
    }

    @Override
    public void publish(Object event) {
        eventListenerManager.notifyListener(event);
    }
}
