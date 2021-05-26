package com.github.rest.proxy.common;

import com.github.rest.proxy.annotation.Flexible;
import com.github.rest.proxy.common.util.Constants;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 配置解析
 */
@SuppressWarnings("rawtypes")
public class FlexibleConfig<R> implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(FlexibleConfig.class);
    private static final long serialVersionUID = 8965666983582088494L;
    private static final Joiner DOT_JOINER = Joiner.on(".");
    public static Map<String, FlexibleConfig> flexibleMap = new ConcurrentHashMap<>(32);

    public static final int DEFAULT_MAX_ATTEMPTS = 1;
    public static final int DEFAULT_RETRY_SLEEP_TIME = 1;

    private transient Retryer<Response<R>> retryer;
    private transient RetrofitCallable<R> callBack;
    private transient Header header;

    public FlexibleConfig() {
    }

    public FlexibleConfig(Retryer<Response<R>> retryer, RetrofitCallable<R> callBack, Header header) {
        this.retryer = retryer;
        this.callBack = callBack;
        this.header = header;
    }

    public FlexibleConfig(Flexible flexible) {
        if (flexible != null) {
            int maxAttempts = ObjectUtils.defaultIfNull(flexible.maxAttempts(), DEFAULT_MAX_ATTEMPTS);
            int retrySleepTime = ObjectUtils.defaultIfNull(flexible.retrySleepTime(), DEFAULT_RETRY_SLEEP_TIME);
            TimeUnit retryUnit = flexible.retryUnit();
            this.retryer = buildRetryer(maxAttempts, retrySleepTime, retryUnit);
            this.callBack = getCallBack(flexible);
            this.header = getHeader(flexible);
        }
    }

    public Retryer<Response<R>> getRetryer() {
        return retryer;
    }

    public RetrofitCallable<R> getCallBack() {
        return callBack;
    }

    public Header getHeader() {
        return header;
    }

    private Retryer<Response<R>> buildRetryer(int maxAttempts, int retrySleepTime, TimeUnit retryUnit) {
        return RetryerBuilder.<Response<R>>newBuilder()
                .retryIfResult(r -> r.code() != Constants.SUCCESS)
                .withStopStrategy(StopStrategies.stopAfterAttempt(maxAttempts))
                .withWaitStrategy(WaitStrategies.fixedWait(retrySleepTime, retryUnit))
                .build();
    }

    private RetrofitCallable getCallBack(Flexible flexible) {
        RetrofitCallable<?> retrofitCallable = (request, response, e) -> {
        };
        Class<? extends RetrofitCallable> callBackClazz = flexible.callBack();
        if (!callBackClazz.isInterface() && RetrofitCallable.class.isAssignableFrom(callBackClazz)) {
            try {
                retrofitCallable = callBackClazz.newInstance();
            } catch (Exception e) {
                log.error("Instantiation CallBack value failed. The class should implement RetrofitCallable interface", e);
            }
        }
        return retrofitCallable;
    }

    private Header getHeader(Flexible flexible) {
        Header header = (headers, arguments) -> Maps.newHashMap();
        Class<? extends Header> headerClazz = flexible.header();
        if (!headerClazz.isInterface() && Header.class.isAssignableFrom(headerClazz)) {
            try {
                header = headerClazz.newInstance();
            } catch (Exception e) {
                log.error("Instantiation header value failed. The class should implement Header interface", e);
            }
        }
        return header;
    }

    private FlexibleConfig<R> defaultConfig() {
        Retryer<Response<R>> r = buildRetryer(DEFAULT_MAX_ATTEMPTS, DEFAULT_RETRY_SLEEP_TIME, TimeUnit.SECONDS);
        RetrofitCallable<R> c = (request, response, e) -> {
        };
        Header h = (headers, arguments) -> Maps.newHashMap();
        return new FlexibleConfig<>(r, c, h);
    }

    public static FlexibleConfig getFlexible(Method method) {
        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String key = DOT_JOINER.join(clazzName, methodName);
        return flexibleMap.getOrDefault(key, new FlexibleConfig<>().defaultConfig());
    }

    public static <T> void flexible(Class<T> clazz) {
        // class annotation
        Arrays.stream(clazz.getAnnotations()).filter(ano -> ano instanceof Flexible).findFirst()
                .ifPresent(annotation -> Arrays.stream(clazz.getMethods()).forEach(method -> {
                    String key = DOT_JOINER.join(clazz.getName(), method.getName());
                    flexibleMap.put(key, new FlexibleConfig((Flexible) annotation));
                }));
        // method annotation
        Arrays.stream(clazz.getMethods()).filter(method -> Objects.nonNull(method.getAnnotation(Flexible.class))).forEach(method -> {
            String key = DOT_JOINER.join(clazz.getName(), method.getName());
            Flexible flexible = method.getAnnotation(Flexible.class);
            flexibleMap.put(key, new FlexibleConfig(flexible));
        });
    }
}