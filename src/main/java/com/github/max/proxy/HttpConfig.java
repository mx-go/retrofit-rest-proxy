package com.github.max.proxy;

import lombok.Data;

/**
 * @author max
 */
@Data
public class HttpConfig {
    /**
     * domain域名
     */
    private String domain;
    /**
     * 读取超时时间
     * 缺省：5000ms
     */
    private Integer readTimeout;
    /**
     * 连接超时时间
     * 缺省：5000ms
     */
    private Integer connectTimeout;
}