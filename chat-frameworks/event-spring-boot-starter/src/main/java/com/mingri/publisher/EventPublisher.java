package com.mingri.publisher;

import com.mingri.event.Event;



public interface EventPublisher {

    /**
     * 发布继承Event类的事件
     */
    <E extends Event> void publish(E event);


    /**
     * 发布任意事件
     */
    void publish(Object event);
}
