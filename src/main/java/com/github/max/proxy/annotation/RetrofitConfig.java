package com.github.max.proxy.annotation;

import java.lang.annotation.*;

/**
 * 注解到接口上。
 *
 * @author max
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RetrofitConfig {
    /**
     * key的名称
     */
    String value();

    /**
     * 描述
     */
    String desc() default "";
}