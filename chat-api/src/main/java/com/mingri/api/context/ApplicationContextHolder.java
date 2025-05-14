package com.mingri.api.context;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 当我们想在非Spring管理的类中获取Spring的bean时，是获取不到的
 * 这就需要我们定义一个自定义的Spring容器上下文，然后进行bean的传递
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    /**
     * Spring 容器在初始化时会调用该方法，将 ApplicationContext 注入到 CONTEXT 中。
     **/
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.CONTEXT = applicationContext;
    }

    /**
     * 根据类型从 Spring 容器中获取 Bean。
     **/
    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT.getBean(clazz);
    }

    /**
     * 根据名称获取 Bean
     **/
    public static Object getBean(String name) {
        return CONTEXT.getBean(name);
    }

    /**
     *  根据名称和类型获取 Bean
     **/
    public static <T> T getBean(String name, Class<T> clazz) {
        return CONTEXT.getBean(name, clazz);
    }

    /**
     *  根据类型获取所有 Bean
     **/
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return CONTEXT.getBeansOfType(clazz);
    }

    /**
     * 查找指定 Bean 上的注解。
     **/
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
        return CONTEXT.findAnnotationOnBean(beanName, annotationType);
    }

    /**
     * 获取保存的 ApplicationContext 实例。
     **/
    public static ApplicationContext getInstance() {
        return CONTEXT;
    }
}
