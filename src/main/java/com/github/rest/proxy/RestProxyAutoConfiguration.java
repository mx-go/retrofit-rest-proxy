package com.github.rest.proxy;

import com.github.rest.proxy.core.ConfigRetrofitSpringFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Create by max on 2021/04/29
 **/
@Configuration
@EnableConfigurationProperties
public class RestProxyAutoConfiguration {

    @Bean
    public ConfigRetrofitSpringFactory configRetrofitSpringFactory(HttpConfig httpConfig) {
        ConfigRetrofitSpringFactory factory = new ConfigRetrofitSpringFactory();
        factory.setHttpConfig(httpConfig);
        return factory;
    }
}