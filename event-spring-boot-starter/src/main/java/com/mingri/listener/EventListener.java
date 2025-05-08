package com.mingri.listener;


import com.mingri.event.Event;


public interface EventListener<E extends Event> {
    /**
     * 触发事件
     */
    void onEvent(E e);

}
