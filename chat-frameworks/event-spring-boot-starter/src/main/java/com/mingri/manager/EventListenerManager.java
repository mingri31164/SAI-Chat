package com.mingri.manager;

import com.mingri.domain.EventListenerRegistration;
import com.mingri.event.Event;
import com.mingri.listener.EventListener;


public interface EventListenerManager {


    /**
     * 注册一个事件监听器
     */
    <E extends Event> void registerListener(Class<? extends Event> clazz, EventListener<E> eventListener);

    /**
     * 通知所有该事件的监听器
     */
    <E extends Event> void notifyListener(E e);

    /**
     * 注册一个事件监听器
     */
    void registerListener(Class<?> eventClazz, EventListenerRegistration listenerRegistration);

    /**
     * 通知所有该事件的监听器
     */
    void notifyListener(Object event);

}
