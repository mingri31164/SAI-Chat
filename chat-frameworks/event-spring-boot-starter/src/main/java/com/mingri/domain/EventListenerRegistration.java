package com.mingri.domain;


public class EventListenerRegistration {

    /**
     * 类
     */
    private Class<?> clazz;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 是否异步执行
     */
    private Boolean async;
    /**
     * 事件监听器对象
     */
    private Object bean;


    public EventListenerRegistration() {
    }

    public EventListenerRegistration(Class<?> clazz, String methodName, Boolean async) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.async = async;
    }

    public EventListenerRegistration(Class<?> clazz, String methodName, Boolean async, Object bean) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.async = async;
        this.bean = bean;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
}
