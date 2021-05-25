package com.github.rest.proxy;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.Map;

/**
 * @author max
 */
@Configuration
@ConfigurationProperties("retrofit.rest.proxy")
public class HttpConfig {

    /**
     * 默认在配置中心key的名称
     */
    public static final String DEFAULT_CONFIG_KEY = "retrofit.config.content";
    /**
     * 配置信息在nacos中的dataId
     */
    private String dataId;
    /**
     * 选填。不填默认取当前spring.application.name
     */
    private String groupId;
    /**
     * 配置信息在配置中心的key
     *
     * @see HttpConfig#DEFAULT_CONFIG_KEY
     */
    private String configKey;
    /**
     * gson是否序列化null属性
     */
    private Boolean serializeNulls;

    public static class Config {
        /**
         * 域名(必填)
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
        /**
         * 请求头配置
         */
        private Map<String, String> headers;
        /**
         * 代理地址
         * e.g：127.0.0.1:8080
         * 为空则不适用代理
         */
        private String proxy;
        /**
         * 需要认证的用户名
         */
        private String userName;
        /**
         * 需要认证的密码
         */
        private String password;

        @Override
        public String toString() {
            return "Config{" +
                    "domain=" + domain +
                    ", readTimeout=" + readTimeout +
                    ", connectTimeout=" + connectTimeout +
                    ", proxy=" + proxy +
                    ", userName=" + userName +
                    ", password=" + password +
                    '}';
        }

        public Pair<String, Integer> getProxy() {
            Pair<String, Integer> result = null;
            if (this.proxy != null) {
                Iterator<String> it = Splitter.on(":").omitEmptyStrings().trimResults().split(this.proxy).iterator();
                result = Pair.of(it.next(), Integer.valueOf(it.next()));
            }
            return result;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public Integer getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
        }

        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Map<String, String> getHeaders() {
            return headers == null ? Maps.newHashMap() : headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public void setProxy(String proxy) {
            this.proxy = proxy;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public Boolean getSerializeNulls() {
        return serializeNulls;
    }

    public void setSerializeNulls(Boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
    }
}