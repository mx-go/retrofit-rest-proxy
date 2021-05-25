package com.github.rest.proxy.common;

import okhttp3.Headers;

import java.util.List;
import java.util.Map;

/**
 * 获取请求头
 * <p>
 * Create by max on 2021/05/22
 **/
public interface Header {

    /**
     * 是否覆盖已有请求头
     *
     * @return true:是。false:否
     */
    default boolean cover() {
        return false;
    }

    /**
     * 获取请求头
     *
     * @param headers   已有的请求头中的数据
     * @param arguments 请求头方法参数
     * @return 请求头
     */
    Map<String, String> getHeader(Headers headers, List<?> arguments);
}