package com.github.proxy;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

@Slf4j
@Data
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
}
