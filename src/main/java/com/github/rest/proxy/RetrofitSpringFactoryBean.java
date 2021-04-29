package com.github.rest.proxy;

import org.springframework.beans.factory.FactoryBean;

public class RetrofitSpringFactoryBean<T> implements FactoryBean<T> {

    /**
     * 创建代理对象的factory
     */
    private RetrofitSpringFactory factory;
    /**
     * 接口路径
     */
    private Class<T> type;

    @Override
    public T getObject() throws Exception {
        return factory.newProxy(type);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public RetrofitSpringFactory getFactory() {
        return factory;
    }

    public void setFactory(RetrofitSpringFactory factory) {
        this.factory = factory;
    }

    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }
}