package com.github.proxy;

/**
 * @author max
 */
public interface RetrofitSpringFactory {

    /**
     * 生成代理对象
     *
     * @param clazz 接口
     * @param <T>   代理对象
     * @return 代理对象
     */
    <T> T newProxy(Class<T> clazz);
}
