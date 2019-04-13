package com.github.max.proxy.core;

import com.github.max.proxy.HttpConfig;
import com.github.max.proxy.RetrofitSpringFactory;
import com.github.max.proxy.annotation.RetrofitConfig;
import com.github.max.proxy.common.LoggingInterceptor;
import com.github.max.proxy.common.utils.UrlUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConfigRetrofitSpringFactory implements RetrofitSpringFactory {

    private static Gson gson = new GsonBuilder().
            registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
                if (src == src.longValue()) {
                    return new JsonPrimitive(src.longValue());
                }
                return new JsonPrimitive(src);
            }).create();

    /**
     * 配置文件所在的路径
     */
    @Setter
    private String configLocations;

    /**
     * 签名需要加密的key
     */
    @Setter
    private String key;

    private volatile Map<String, Retrofit> retrofitMap = Maps.newConcurrentMap();

    /**
     * 初始化
     */
    public void init() {
        Preconditions.checkArgument(StringUtils.isNotBlank(configLocations), "configLocations can not be empty");
        List<String> locations = Splitter.on(",").splitToList(configLocations);
        locations.forEach(this::add2RetrofitMap);
    }

    private void add2RetrofitMap(String location) {

        // 获取配置内容
        String content = this.getConfigContent(location);

        Map<String, HttpConfig> configMap = gson.fromJson(content, new TypeToken<Map<String, HttpConfig>>() {
        }.getType());

        for (Entry<String, HttpConfig> entry : configMap.entrySet()) {
            if (retrofitMap.containsKey(entry.getKey())) {
                log.warn("{} already add, please check your configLocations or configuration.", entry.getKey());
            }

            HttpConfig httpConfig = entry.getValue();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            int readTimeout = 5000;
            int connectTimeout = 5000;
            if (httpConfig.getReadTimeout() != null) {
                readTimeout = httpConfig.getReadTimeout();
            }
            if (httpConfig.getConnectTimeout() != null) {
                connectTimeout = httpConfig.getConnectTimeout();
            }
            builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
            builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            builder.addInterceptor(new LoggingInterceptor(key));
            OkHttpClient okHttpClient = builder.build();
            String baseUrl = UrlUtils.getServiceUrl(httpConfig.getDomain());

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(new CallAdapterFactoryCore())
                    .client(okHttpClient).build();
            retrofitMap.put(entry.getKey(), retrofit);
        }
    }

    /**
     * 获取配置信息
     *
     * @return content
     */
    private String getConfigContent(String location) {
        String content = "";
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources(location);
            for (Resource resource : resources) {
                content = IOUtils.toString(resource.getInputStream(), "utf-8");
            }
            if (Strings.isNullOrEmpty(content)) {
                throw new IllegalArgumentException("rest-proxy content is empty");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    @Override
    public <T> T newProxy(Class<T> clazz) {
        RetrofitConfig config = clazz.getAnnotation(RetrofitConfig.class);
        Retrofit retrofit = retrofitMap.get(config.value());
        if (retrofit == null) {
            throw new IllegalArgumentException(clazz.getName() + " RetrofitConfig baseUrl not found config");
        }
        return retrofit.create(clazz);
    }
}
