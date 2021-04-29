package com.github.rest.proxy.common;

import com.github.rest.proxy.annotation.Flexible;
import com.github.rest.proxy.common.util.Constants;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class FlexibleConfig<R> implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(FlexibleConfig.class);

    private static final long serialVersionUID = 8965666983582088494L;

    public static final int DEFAULT_MAX_ATTEMPTS = 1;
    public static final int DEFAULT_RETRY_SLEEP_TIME = 1;

    private transient Retryer<Response<R>> retryer;
    private transient RetrofitCallable<R> callBackClazz;

    public FlexibleConfig() {
    }

    public FlexibleConfig(Retryer<Response<R>> retryer, RetrofitCallable<R> callBackClazz) {
        this.retryer = retryer;
        this.callBackClazz = callBackClazz;
    }

    public FlexibleConfig(Flexible flexible) {
        int maxAttempts = ObjectUtils.defaultIfNull(flexible.maxAttempts(), DEFAULT_MAX_ATTEMPTS);
        int retrySleepTime = ObjectUtils.defaultIfNull(flexible.retrySleepTime(), DEFAULT_RETRY_SLEEP_TIME);
        TimeUnit retryUnit = flexible.retryUnit();
        this.retryer = buildRetryer(maxAttempts, retrySleepTime, retryUnit);
        this.callBackClazz = getCallBackClazz(flexible);
    }

    public Retryer<Response<R>> getRetryer() {
        return retryer;
    }

    public RetrofitCallable<R> getCallBackClazz() {
        return callBackClazz;
    }

    private RetrofitCallable getCallBackClazz(Flexible flexible) {
        RetrofitCallable<?> retrofitCallable = (request, response, e) -> {
        };
        if (flexible != null && RetrofitCallable.class.isAssignableFrom(flexible.callBackClazz())) {
            try {
                retrofitCallable = (RetrofitCallable<?>) flexible.callBackClazz().newInstance();
            } catch (Exception e) {
                log.error("Instantiation CallBack value failed. The class should implement RetrofitCallable interface", e);
            }
        }
        return retrofitCallable;
    }

    public FlexibleConfig<R> defaultConfig() {
        Retryer<Response<R>> r = buildRetryer(DEFAULT_MAX_ATTEMPTS, DEFAULT_RETRY_SLEEP_TIME, TimeUnit.SECONDS);
        RetrofitCallable<R> c = (request, response, e) -> {
        };
        return new FlexibleConfig<>(r, c);
    }

    private Retryer<Response<R>> buildRetryer(int maxAttempts, int retrySleepTime, TimeUnit retryUnit) {
        return RetryerBuilder.<Response<R>>newBuilder()
                .retryIfResult(r -> r.code() != Constants.SUCCESS)
                .withStopStrategy(StopStrategies.stopAfterAttempt(maxAttempts))
                .withWaitStrategy(WaitStrategies.fixedWait(retrySleepTime, retryUnit))
                .build();
    }
}