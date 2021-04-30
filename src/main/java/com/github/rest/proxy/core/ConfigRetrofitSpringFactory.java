package com.github.rest.proxy.core;

import com.github.mx.nacos.config.core.ConfigFactory;
import com.github.mx.nacos.config.core.RemoteConfig;
import com.github.mx.nacos.config.core.annotation.RefreshConfig;
import com.github.rest.proxy.HttpConfig;
import com.github.rest.proxy.HttpConfig.Config;
import com.github.rest.proxy.RetrofitSpringFactory;
import com.github.rest.proxy.annotation.Flexible;
import com.github.rest.proxy.annotation.RetrofitConfig;
import com.github.rest.proxy.common.FlexibleConfig;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.reflect.Reflection;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
@RefreshConfig
public class ConfigRetrofitSpringFactory implements RetrofitSpringFactory {

    private static final Logger log = LoggerFactory.getLogger(ConfigRetrofitSpringFactory.class);

    private ConcurrentHashMap<String, ConcurrentHashMap<Class, RestInvocationHandler>> handlerMap;
    static Map<String, FlexibleConfig> flexibleMap;
    private Map<String, Config> configMap;
    private Gson gson;

    /**
     * HTTP请求配置的内容
     */
    private HttpConfig httpConfig;

    @PostConstruct
    public void init() {
        this.buildGson(httpConfig.getSerializeNulls());
        handlerMap = new ConcurrentHashMap<>(16);
        flexibleMap = new ConcurrentHashMap<>(32);
        configMap = Maps.newHashMap();
        String dataId = httpConfig.getDataId();
        String groupId = httpConfig.getGroupId();

        if (StringUtils.isAllBlank(dataId, groupId)) {
            log.warn("Init ConfigRetrofitSpringFactory failed. dataId and groupId are emtpy");
            return;
        }

        if (StringUtils.isBlank(groupId)) {
            ConfigFactory.getInstance().registerListener(dataId, configInfo -> process(dataId, configInfo));
        } else {
            ConfigFactory.getInstance().registerListener(dataId, groupId, configInfo -> process(dataId, configInfo));
        }
    }

    private void process(String dataId, String config) {
        String configKey = StringUtils.defaultIfBlank(httpConfig.getConfigKey(), HttpConfig.DEFAULT_CONFIG_KEY);
        // dataId和configKey相同时取content
        if (!dataId.equals(configKey)) {
            config = RemoteConfig.convert(config).get(configKey);
        }
        this.configChange(config);
    }

    private void configChange(String content) {
        Map<String, Config> newConfigMap = gson.fromJson(content, new TypeToken<Map<String, Config>>() {
        }.getType());
        for (Map.Entry<String, Config> entry : newConfigMap.entrySet()) {
            String key = entry.getKey();
            Config value = entry.getValue();
            configMap.put(key, value);

            ConcurrentHashMap<Class, RestInvocationHandler> handlers = handlerMap.get(key);
            if (handlers != null) {
                for (Map.Entry<Class, RestInvocationHandler> handlerEntry : handlers.entrySet()) {
                    RestInvocationHandler handler = handlerEntry.getValue();
                    handler.initOrReload(value);
                    log.info("reload {}-{}, config={}", handlerEntry.getKey(), handler, value);
                }
            }
        }
    }

    private void buildGson(Boolean serializeNulls) {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
            @Override
            public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                if (src == null) {
                    return JsonNull.INSTANCE;
                }
                return new JsonPrimitive(new BigDecimal(src) {
                    private static final long serialVersionUID = -1914748697243137571L;

                    @Override
                    public String toString() {
                        return super.toPlainString();
                    }
                });
            }
        });
        if (serializeNulls != null && serializeNulls) {
            builder.serializeNulls();
        }
        gson = builder.create();
    }

    @Override
    public <T> T newProxy(Class<T> clazz) {
        RetrofitConfig retrofitConfig = clazz.getAnnotation(RetrofitConfig.class);
        Config config = configMap.get(retrofitConfig.value());
        if (config == null) {
            log.error("{} RetrofitConfig baseUrl not found config", clazz.getName());
            return null;
        }

        ConcurrentHashMap<Class, RestInvocationHandler> handlers = handlerMap.get(retrofitConfig.value());
        if (handlers == null) {
            handlers = new ConcurrentHashMap<>(16);
            ConcurrentHashMap<Class, RestInvocationHandler> oldHandlers = handlerMap.putIfAbsent(retrofitConfig.value(), handlers);
            if (oldHandlers != null) {
                handlers = oldHandlers;
            }
        }
        RestInvocationHandler handler = handlers.get(clazz);
        if (handler == null) {
            handler = new RestInvocationHandler<>(clazz, gson);
            RestInvocationHandler oldHandler = handlers.putIfAbsent(clazz, handler);
            if (oldHandler != null) {
                handler = oldHandler;
            }
            handler.initOrReload(config);
            this.flexible(clazz);
        }
        log.info("new proxy, {}-{}", clazz, handler);
        return Reflection.newProxy(clazz, handler);
    }

    private <T> void flexible(Class<T> clazz) {
        Joiner joiner = Joiner.on(".");
        // class annotation
        Arrays.stream(clazz.getAnnotations()).filter(ano -> ano instanceof Flexible).findFirst()
                .ifPresent(annotation -> Arrays.stream(clazz.getMethods()).forEach(method -> {
                    String key = joiner.join(clazz.getName(), method.getName());
                    flexibleMap.put(key, new FlexibleConfig((Flexible) annotation));
                }));
        // method annotation
        Arrays.stream(clazz.getMethods()).filter(method -> Objects.nonNull(method.getAnnotation(Flexible.class))).forEach(method -> {
            String key = joiner.join(clazz.getName(), method.getName());
            Flexible flexible = method.getAnnotation(Flexible.class);
            flexibleMap.put(key, new FlexibleConfig(flexible));
        });
    }

    public void setHttpConfig(HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
    }
}