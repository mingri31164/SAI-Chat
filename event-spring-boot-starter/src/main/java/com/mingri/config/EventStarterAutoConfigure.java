package com.mingri.config;

import com.mingri.listener.OnceApplicationContextEventListener;
import com.mingri.manager.EventListenerManager;
import com.mingri.manager.impl.DefaultEventListenerManager;
import com.mingri.publisher.EventPublisher;
import com.mingri.publisher.impl.DefaultEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class EventStarterAutoConfigure {

    @Bean
    public EventListenerManager eventListenerManager() {
        return new DefaultEventListenerManager();
    }

    @Bean
    public EventPublisher eventPublisher(@Autowired EventListenerManager eventListenerManager) {
        return new DefaultEventPublisher(eventListenerManager);
    }

    @Bean
    public OnceApplicationContextEventListener onceApplicationListener() {
        return new OnceApplicationContextEventListener();
    }
}
