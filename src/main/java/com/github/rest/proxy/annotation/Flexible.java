package com.github.rest.proxy.annotation;

import com.github.rest.proxy.common.FlexibleConfig;
import com.github.rest.proxy.common.Header;
import com.github.rest.proxy.common.RetrofitCallable;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Annotate a class or method to enhance its use. include retry and callback
 * <p>
 * Create by max(01399850) on 2021/04/12
 **/
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Flexible {

    /**
     * @return the maximum number of attempts (including the first failure), defaults to 1(gt 0)
     */
    int maxAttempts() default FlexibleConfig.DEFAULT_MAX_ATTEMPTS;

    /**
     * @return retrySleepTime the time to sleep when retry
     */
    int retrySleepTime() default FlexibleConfig.DEFAULT_RETRY_SLEEP_TIME;

    /**
     * @return retryUnit retry unit
     */
    TimeUnit retryUnit() default TimeUnit.SECONDS;

    /**
     * @return 回调的类(需实现RetrofitCallable接口)
     */
    Class<? extends RetrofitCallable> callBack() default RetrofitCallable.class;

    /**
     * @return 获取请求头的类(需实现Header接口)
     */
    Class<? extends Header> header() default Header.class;
}